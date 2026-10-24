package com.xiaohe.english.utils

import android.content.Context
import android.os.Build
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.concurrent.thread

/** 崩溃日志收集与 OSS 直传 —— 与原版 CrashLogUploader.kt 相同的行为。 */
object CrashLogUploader {
    private const val TAG = "CrashLogUploader"
    private const val CRASH_LOG_DIR = "crash_logs"
    private const val MAX_LOG_FILES = 20
    private const val MAX_RETRY_COUNT = 3

    private var appContext: Context? = null
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    private var userId: String? = null
    private var userName: String? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e(TAG, "捕获到未处理异常: ${throwable.message}", throwable)
                saveCrashLog(thread, throwable)
            } catch (e: Exception) {
                Log.e(TAG, "保存崩溃日志时发生错误: ${e.message}")
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
        Log.d(TAG, "崩溃日志收集器已初始化")
        uploadPendingCrashLogs()
    }

    fun setUserInfo(id: String?, name: String?) {
        userId = id
        userName = name
    }

    fun clearUserInfo() {
        userId = null
        userName = null
    }

    private fun getCrashLogDir(): File {
        val ctx = appContext ?: error("not initialized")
        val dir = File(ctx.filesDir, CRASH_LOG_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun saveCrashLog(thread: Thread, throwable: Throwable) {
        try {
            val dir = getCrashLogDir()
            cleanOldLogs(dir)
            val file = File(dir, "crash_" + SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.CHINA).format(Date()) + ".json")
            file.writeText(buildCrashLogContent(thread, throwable))
            Log.d(TAG, "崩溃日志已保存: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "保存崩溃日志失败: ${e.message}")
        }
    }

    private fun cleanOldLogs(dir: File) {
        val files = dir.listFiles { f -> f.name.endsWith(".json") } ?: return
        val sorted = files.sortedByDescending { it.lastModified() }
        if (sorted.size >= MAX_LOG_FILES) {
            sorted.drop(MAX_LOG_FILES - 1).forEach { it.delete() }
        }
    }

    private fun uploadPendingCrashLogs() {
        thread(name = "crash-upload") {
            val files = getCrashLogDir().listFiles { f -> f.name.endsWith(".json") } ?: return@thread
            for (file in files) uploadCrashLogFile(file)
        }
    }

    private fun uploadCrashLogFile(file: File) {
        try {
            val json = file.readText()
            val obj = JSONObject(json)
            val retryCount = obj.optInt("uploadRetryCount", 0)
            if (retryCount >= MAX_RETRY_COUNT) {
                Log.w(TAG, "崩溃日志重试次数超限，删除: ${file.name}")
                file.delete()
                return
            }
            obj.put("uploadRetryCount", retryCount + 1)
            val updated = obj.toString(2)
            file.writeText(updated)
            val objectName = "android/crash/" + file.nameWithoutExtension + "_" + UUID.randomUUID().toString().take(8) + ".json"
            if (!OssDirectUploader.uploadToOss(objectName, updated)) {
                Log.w(TAG, "崩溃日志上传失败，保留文件等待下次重试: ${file.name}")
                return
            }
            Log.d(TAG, "崩溃日志上传成功，删除本地文件: ${file.name}")
            file.delete()
        } catch (e: Exception) {
            Log.e(TAG, "处理崩溃日志文件失败: ${file.name}, ${e.message}")
        }
    }

    private fun buildCrashLogContent(thread: Thread, throwable: Throwable): String {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val ctx = appContext
        val pm = ctx?.packageManager
        val pkgInfo = ctx?.let { runCatching { pm?.getPackageInfo(it.packageName, 0) }.getOrNull() }
        return JSONObject().apply {
            put("platform", "android")
            put("crashId", UUID.randomUUID().toString())
            put("timestamp", System.currentTimeMillis())
            put("datetime", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date()))
            put("deviceInfo", JSONObject().apply {
                put("manufacturer", Build.MANUFACTURER)
                put("model", Build.MODEL)
                put("brand", Build.BRAND)
                put("androidVersion", Build.VERSION.RELEASE)
                put("sdkInt", Build.VERSION.SDK_INT)
                put("device_info", Build.DEVICE)
                put("product", Build.PRODUCT)
            })
            put("appInfo", JSONObject().apply {
                put("versionName", pkgInfo?.versionName ?: "")
                put("versionCode", pkgInfo?.let { if (Build.VERSION.SDK_INT >= 28) it.longVersionCode else it.versionCode.toLong() } ?: 0L)
                put("packageName", ctx?.packageName ?: "")
            })
            put("userInfo", JSONObject().apply {
                put("userId", userId ?: "")
                put("userName", userName ?: "")
            })
            put("threadInfo", JSONObject().apply {
                put("name", thread.name)
                put("id", thread.id)
                put("priority", thread.priority)
                put("state", thread.state.name)
            })
            put("exception", JSONObject().apply {
                put("type", throwable.javaClass.name)
                put("message", throwable.message ?: "")
                put("stackTrace", sw.toString())
                throwable.cause?.let { cause ->
                    put("cause", JSONObject().apply {
                        put("type", cause.javaClass.name)
                        put("message", cause.message ?: "")
                    })
                }
            })
            put("memoryInfo", JSONObject().apply {
                val rt = Runtime.getRuntime()
                put("maxMemory", rt.maxMemory())
                put("totalMemory", rt.totalMemory())
                put("freeMemory", rt.freeMemory())
            })
        }.toString(2)
    }
}