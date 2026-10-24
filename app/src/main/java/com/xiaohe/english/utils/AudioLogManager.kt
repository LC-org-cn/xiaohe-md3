package com.xiaohe.english.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 音频评测步骤日志（本地文件）—— 与原版 AudioLogManager.kt 一致。 */
object AudioLogManager {
    private const val TAG = "AudioLogManager"
    private const val MAX_LOG_SIZE = 2 * 1024 * 1024
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA)

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        rotateLogIfNeeded()
    }

    fun log(step: String, questionId: String, success: Boolean, detail: String) {
        val line = "[${dateFormat.format(Date())}] [${if (success) "✅ SUCCESS" else "❌ FAILED"}] [step=$step] [qid=$questionId] $detail"
        if (success) Log.i(TAG, line) else Log.e(TAG, line)
        writeToFile(line)
    }

    fun logSuccess(step: String, questionId: String, detail: String) = log(step, questionId, true, detail)

    fun logFailure(step: String, questionId: String, detail: String) = log(step, questionId, false, detail)

    private fun getLogFile(): File? {
        val ctx = appContext ?: return null
        val dir = File(ctx.filesDir, "audio_logs")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "audio_eval_log.txt")
    }

    private fun rotateLogIfNeeded() {
        val file = getLogFile() ?: return
        if (file.exists() && file.length() > MAX_LOG_SIZE) {
            val old = File(file.parentFile, "audio_eval_log_old.txt")
            old.delete()
            file.renameTo(old)
        }
    }

    private fun writeToFile(line: String) {
        try {
            getLogFile()?.appendText(line + "\n")
        } catch (e: Exception) {
            Log.e(TAG, "写日志失败: ${e.message}")
        }
    }
}