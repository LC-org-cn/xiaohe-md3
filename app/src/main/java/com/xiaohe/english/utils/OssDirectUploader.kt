package com.xiaohe.english.utils

import android.util.Base64
import android.util.Log
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 阿里云 OSS 直传 —— 与原版 CrashLogUploader/EvaluationLogUploader 相同的
 * 签名算法与请求头（HMAC-SHA1 + PUT + application/json），同一 bucket。
 */
object OssDirectUploader {
    private const val TAG = "OssDirectUploader"
    // 凭证与原版一致，经 CI Secret 注入（见 build.yml OSS_AK_ID/OSS_AK_SECRET），本地调试可用 local.properties
    private val accessKeyId: String by lazy { com.xiaohe.english.BuildConfig.OSS_AK_ID }
    private val accessKeySecret: String by lazy { com.xiaohe.english.BuildConfig.OSS_AK_SECRET }
    private const val ENDPOINT = "sidlog.oss-cn-beijing.aliyuncs.com"

    fun uploadToOss(objectName: String, content: String): Boolean = try {
        val bytes = content.toByteArray(StandardCharsets.UTF_8)
        val gmt = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("GMT")
        }.format(Date())
        val stringToSign = "PUT\n\napplication/json\n$gmt\n/sidlog/$objectName"
        val signature = calculateSignature(stringToSign, ACCESS_KEY_SECRET)
        val authorization = "OSS $accessKeyId:$signature"

        val conn = URL("https://$ENDPOINT/$objectName").openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "PUT"
            conn.doOutput = true
            conn.connectTimeout = 30000
            conn.readTimeout = 30000
            conn.setRequestProperty("Date", gmt)
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Content-Length", bytes.size.toString())
            conn.setRequestProperty("Authorization", authorization)
            conn.setRequestProperty("Host", ENDPOINT)
            OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use { it.write(content) }
            val code = conn.responseCode
            if (code in 200..299) {
                Log.d(TAG, "OSS上传成功: $objectName")
                true
            } else {
                Log.e(TAG, "OSS上传失败: responseCode=$code")
                false
            }
        } finally {
            conn.disconnect()
        }
    } catch (e: Exception) {
        Log.e(TAG, "OSS上传异常: ${e.message}")
        false
    }

    private fun calculateSignature(stringToSign: String, secretKey: String): String {
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(secretKey.toByteArray(StandardCharsets.UTF_8), "HmacSHA1"))
        return Base64.encodeToString(mac.doFinal(stringToSign.toByteArray(StandardCharsets.UTF_8)), Base64.NO_WRAP)
    }
}