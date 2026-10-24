package com.xiaohe.english.speech

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.iflytek.msc.MSC
import com.iflytek.msc.MSCSessionInfo
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * 讯飞语音评测引擎（直接 JNI 驱动 libmsc.so 的 QISE* API）。
 *
 * 参数序列复刻自原版 APK AudioEvaluationManager.smali：
 * sub=ise, ent=cn_vip/en_vip, plev=pupil, ise_unite=entirety,
 * extra_ability=multi_dimension, result_level=complete, result_type=xml,
 * text_encoding=utf-8, vad_* = 999999, sample_rate=16000, aue=raw。
 * SDK 默认补充（h9/c.smali）：ssm=1, accent=mandarin, domain=ise, subject=ise,
 * auf=audio/L16;rate=16000, vad_speech_timeout=2147483647。
 */
object MscEngine {
    private const val TAG = "MscEngine"
    const val APP_ID = "e10cf2cc"

    @Volatile private var loggedIn = false

    @Synchronized
    fun ensureLogin(): Int {
        if (loggedIn) return 0
        val params = ("appid=" + APP_ID).toByteArray(Charsets.UTF_8)
        val ret = MSC.QMSPLogin(null, null, params)
        loggedIn = ret == 0
        Log.d(TAG, "QMSPLogin ret=$ret")
        return ret
    }

    @Synchronized
    fun logout() {
        if (loggedIn) {
            MSC.QMSPLogout()
            loggedIn = false
        }
    }

    fun buildParams(language: String, category: String): String {
        val ent = if (language == "zh_cn") "cn_vip" else "en_vip"
        return listOf(
            "sub=ise",
            "ent=$ent",
            "language=$language",
            "category=$category",
            "plev=pupil",
            "ise_unite=entirety",
            "extra_ability=multi_dimension",
            "text_encoding=utf-8",
            "result_type=xml",
            "result_level=complete",
            "vad_enable=1",
            "vad_bos=999999",
            "vad_eos=999999",
            "vad_timeout=999999",
            "vad_speech_tail=999999",
            "aue=raw",
            "auf=audio/L16;rate=16000",
            "sample_rate=16000",
            "ssm=1",
            "accent=mandarin",
            "domain=ise",
            "subject=ise",
            "vad_speech_timeout=2147483647",
            "engine_type=cloud",
        ).joinToString(",")
    }
}

sealed class EvalEvent {
    data class Volume(val volume: Int) : EvalEvent()
    data class PartialResult(val xml: String) : EvalEvent()
    data class FinalResult(val xml: String) : EvalEvent()
    data class Error(val code: Int, val message: String) : EvalEvent()
    data class State(val running: Boolean) : EvalEvent()
}

class EvaluationSession(
    private val language: String = "en_us",
    private val category: String = "read_sentence",
) {
    companion object {
        private const val TAG = "EvaluationSession"
        private const val SAMPLE_RATE = 16000
        private const val AUDIO_STATUS_CONTINUE = 0
        private const val AUDIO_STATUS_LAST = 2
        // info.rsltstatus: 0 no result yet, 1 has partial, 2 final
        private const val RSLT_STATUS_FINAL = 2
    }

    private var sessionId: CharArray? = null
    private var recorder: AudioRecord? = null
    private val running = AtomicBoolean(false)
    private var minBuf = 0

    @Volatile var lastError: Pair<Int, String>? = null
        private set

    /** Start evaluation: logs in, opens session, records mic and streams PCM to the engine. */
    @SuppressLint("MissingPermission")
    fun start(text: String, onEvent: (EvalEvent) -> Unit) {
        if (running.get()) { onEvent(EvalEvent.Error(-1, "评测已在进行")); return }
        val loginRet = MscEngine.ensureLogin()
        if (loginRet != 0) { onEvent(EvalEvent.Error(loginRet, "MSPLogin 失败")); return }

        val info = MSCSessionInfo()
        val textBytes = text.toByteArray(Charsets.UTF_8)
        val paramsBytes = MscEngine.buildParams(language, category).toByteArray(Charsets.UTF_8)

        val sid = MSC.QISESessionBegin(textBytes, paramsBytes, info)
        if (sid == null || info.errorcode != 0) {
            onEvent(EvalEvent.Error(info.errorcode, "QISESessionBegin 失败"))
            return
        }
        sessionId = sid
        running.set(true)
        onEvent(EvalEvent.State(true))

        thread(name = "ise-eval") {
            try {
                minBuf = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
                )
                val rec = AudioRecord(
                    MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    maxOf(minBuf, 3200)
                )
                recorder = rec
                rec.startRecording()
                val buf = ByteArray(3200) // 100ms of 16k mono 16bit
                while (running.get()) {
                    val n = rec.read(buf, 0, buf.size)
                    if (n > 0) {
                        val wInfo = MSCSessionInfo()
                        val wRet = MSC.QISEAudioWrite(sid, buf, n, AUDIO_STATUS_CONTINUE, wInfo)
                        if (wRet != 0) {
                            onEvent(EvalEvent.Error(wRet, "QISEAudioWrite 失败"))
                            break
                        }
                        // volume estimation (RMS)
                        var sum = 0.0
                        var i = 0
                        while (i + 1 < n) {
                            val v = ((buf[i + 1].toInt() shl 8) or (buf[i].toInt() and 0xFF)).toShort()
                            sum += v.toDouble() * v
                            i += 2
                        }
                        val rms = Math.sqrt(sum / (n / 2.0))
                        onEvent(EvalEvent.Volume((rms / 200).toInt().coerceIn(0, 30)))
                    }
                    // poll partial results
                    val rInfo = MSCSessionInfo()
                    val r = MSC.QISEGetResult(sid, rInfo)
                    if (r != null && r.isNotEmpty() && rInfo.errorcode == 0) {
                        onEvent(EvalEvent.PartialResult(String(r, Charsets.UTF_8)))
                    }
                    if (rInfo.rsltstatus == RSLT_STATUS_FINAL) {
                        val f = String(r ?: ByteArray(0), Charsets.UTF_8)
                        onEvent(EvalEvent.FinalResult(f))
                        break
                    }
                }
                rec.stop()
                rec.release()
                recorder = null

                // signal end-of-audio then collect final result
                val endInfo = MSCSessionInfo()
                val empty = ByteArray(0)
                MSC.QISEAudioWrite(sid, empty, 0, AUDIO_STATUS_LAST, endInfo)
                // poll final result
                var tries = 0
                while (tries < 100) {
                    val rInfo = MSCSessionInfo()
                    val r = MSC.QISEGetResult(sid, rInfo)
                    if (r != null && r.isNotEmpty() && rInfo.errorcode == 0) {
                        if (rInfo.rsltstatus == RSLT_STATUS_FINAL) {
                            onEvent(EvalEvent.FinalResult(String(r, Charsets.UTF_8)))
                            break
                        }
                        // still partial
                    }
                    if (rInfo.rsltstatus == RSLT_STATUS_FINAL) break
                    if (rInfo.errorcode != 0) {
                        onEvent(EvalEvent.Error(rInfo.errorcode, "QISEGetResult 失败"))
                        break
                    }
                    tries++
                    Thread.sleep(100)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "eval thread", t)
                onEvent(EvalEvent.Error(-2, t.message ?: "录音异常"))
            } finally {
                sessionId?.let { MSC.QISESessionEnd(it, "eval done".toByteArray()) }
                sessionId = null
                running.set(false)
                onEvent(EvalEvent.State(false))
            }
        }
    }

    /** Stop recording; engine finalizes and returns final xml via FinalResult. */
    fun stop() {
        running.set(false)
    }

    fun cancel() {
        running.set(false)
        try {
            recorder?.stop()
        } catch (_: Exception) {}
        sessionId?.let { MSC.QISESessionEnd(it, "cancelled".toByteArray()) }
        sessionId = null
    }

    val isRunning: Boolean get() = running.get()
}

/** Parse the ISE XML result into scores. */
object EvalResultParser {
    data class Scores(
        val total: Float?,
        val pronunciation: Float?,
        val fluency: Float?,
        val integrity: Float?,
    )

    fun parse(xml: String): Scores {
        val total = attr(xml, "total_score")
        val phone = attr(xml, "phone_score") ?: attr(xml, "pronunciation_score")
        val fluency = attr(xml, "fluency_score") ?: attr(xml, "tone_score")
        val integrity = attr(xml, "integrity_score") ?: attr(xml, "content_score")
        return Scores(total, phone, fluency, integrity)
    }

    private fun attr(xml: String, name: String): Float? {
        val re = Regex("$name=\"([^\"]*)\"")
        val m = re.find(xml) ?: return null
        return m.groupValues[1].toFloatOrNull()
    }
}
