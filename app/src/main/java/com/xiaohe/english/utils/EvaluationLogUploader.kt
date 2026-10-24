package com.xiaohe.english.utils

import android.os.Build
import android.util.Log
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

/** 评测日志 OSS 直传 —— 与原版 EvaluationLogUploader.kt 相同的两种日志。 */
object EvaluationLogUploader {
    private const val TAG = "EvaluationLogUploader"

    data class ExtraInfo(
        val evaluationId: String = "",
        val userId: String = "",
        val userName: String = "",
        val questionId: String = "",
        val questionTitle: String = "",
        val questionType: String = "",
        val exerciseId: String = "",
        val exerciseName: String = "",
        val audioRecordUrl: String = "",
        val localAudioPath: String = "",
        val xunfeiSid: String = "",
    )

    fun getDeviceInfo(): String = try {
        "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, SDK ${Build.VERSION.SDK_INT})"
    } catch (e: Exception) {
        "Unknown Device"
    }

    /** 零分/异常评测日志。params 为附加上下文（如 xml 片段、sid）。 */
    fun uploadZeroScoreLog(
        params: Map<String, Any?>,
        text: String,
        score: Double,
        deviceInfo: String,
        extraInfo: ExtraInfo,
    ) {
        thread(name = "eval-log") {
            try {
                val json = JSONObject().apply {
                    put("platform", "android")
                    put("logType", "zero_score")
                    put("timestamp", System.currentTimeMillis())
                    put("datetime", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date()))
                    put("text", text)
                    put("score", score)
                    put("deviceInfo", deviceInfo)
                    put("params", JSONObject(params))
                    put("evaluationId", extraInfo.evaluationId)
                    put("userId", extraInfo.userId)
                    put("userName", extraInfo.userName)
                    put("questionId", extraInfo.questionId)
                    put("questionTitle", extraInfo.questionTitle)
                    put("questionType", extraInfo.questionType)
                    put("exerciseId", extraInfo.exerciseId)
                    put("exerciseName", extraInfo.exerciseName)
                    put("audioRecordUrl", extraInfo.audioRecordUrl)
                    put("localAudioPath", extraInfo.localAudioPath)
                    put("xunfeiSid", extraInfo.xunfeiSid)
                }.toString(2)
                val objectName = "android/zero_score_" + SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.CHINA).format(Date()) + "_" + extraInfo.evaluationId.take(8) + ".json"
                OssDirectUploader.uploadToOss(objectName, json)
                Log.d(TAG, "零分日志上传完成: $objectName")
            } catch (e: Exception) {
                Log.e(TAG, "上传零分日志失败: ${e.message}", e)
            }
        }
    }

    /** 音频 URL 补充更新日志。 */
    fun uploadAudioUrlUpdate(
        evaluationId: String,
        questionId: String?,
        localAudioPath: String?,
        remoteAudioUrl: String?,
    ) {
        thread(name = "eval-log") {
            try {
                Log.d(TAG, "补充上传音频URL更新日志: evaluationId=$evaluationId")
                val json = JSONObject().apply {
                    put("platform", "android")
                    put("logType", "audio_url_update")
                    put("evaluationId", evaluationId)
                    put("timestamp", System.currentTimeMillis())
                    put("datetime", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date()))
                    put("questionId", questionId ?: "")
                    put("localAudioPath", localAudioPath ?: "")
                    put("remoteAudioUrl", remoteAudioUrl ?: "")
                }.toString(2)
                val objectName = "android/audio_url_update_" + SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.CHINA).format(Date()) + "_" + evaluationId.take(8) + ".json"
                OssDirectUploader.uploadToOss(objectName, json)
                Log.d(TAG, "音频URL更新日志上传成功: $objectName")
            } catch (e: Exception) {
                Log.e(TAG, "上传音频URL更新日志失败: ${e.message}", e)
            }
        }
    }
}