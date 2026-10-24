package com.xiaohe.english.wechat

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * 微信 SDK 封装 —— 与原版 WeChatManager.kt 逐行对应。
 * AppID、缩略图压缩、transaction 格式全部一致。
 */
object WeChatManager {
    private const val TAG = "WeChatManager"
    const val WX_APP_ID = "wx7eff6dad4cfd221a"

    @Volatile private var wxApi: IWXAPI? = null

    fun init(context: Context): Boolean = try {
        val api = WXAPIFactory.createWXAPI(context, WX_APP_ID, true)
        wxApi = api
        val ok = api?.registerApp(WX_APP_ID) ?: false
        if (ok) Log.d(TAG, "微信SDK初始化成功") else Log.e(TAG, "微信SDK注册失败")
        ok
    } catch (e: Exception) {
        Log.e(TAG, "微信SDK初始化异常: ${e.message}", e)
        false
    }

    fun isWeChatInstalled(): Boolean = wxApi?.isWXAppInstalled ?: false

    fun handleIntent(intent: Intent, handler: IWXAPIEventHandler) {
        wxApi?.handleIntent(intent, handler)
    }

    /** 异步分享网页：先下载原图并压缩到 32KB 内，再调起微信。 */
    fun shareWebPage(
        context: Context,
        title: String,
        description: String,
        url: String,
        imageUrl: String?,
        scene: Int = 0,
        onResult: (Boolean) -> Unit = {},
    ) {
        val api = wxApi
        if (api == null) { Log.e(TAG, "微信SDK未初始化"); onResult(false); return }
        if (!isWeChatInstalled()) { Log.e(TAG, "微信未安装"); onResult(false); return }

        thread(name = "wx-share") {
            val msg = WXMediaMessage(WXWebpageObject().apply { webpageUrl = url })
            msg.title = title
            msg.description = description
            if (!imageUrl.isNullOrEmpty()) {
                val bmp = downloadBitmap(imageUrl)
                if (bmp != null) {
                    msg.thumbData = compressThumb(bmp, 32 * 1024)
                } else {
                    setDefaultThumbnail(msg)
                }
            } else {
                Log.w(TAG, "未提供图片URL，使用默认缩略图")
                setDefaultThumbnail(msg)
            }
            val req = SendMessageToWX.Req().apply {
                transaction = "webpage_${System.currentTimeMillis()}"
                message = msg
                this.scene = scene
            }
            val ok = api.sendReq(req)
            Log.d(TAG, "sendReq: $ok")
            android.os.Handler(context.mainLooper).post { onResult(ok) }
        }
    }

    private fun setDefaultThumbnail(msg: WXMediaMessage) = try {
        val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val bos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        msg.thumbData = bos.toByteArray()
    } catch (e: Exception) {
        Log.e(TAG, "设置默认缩略图失败: ${e.message}")
    }

    private fun compressThumb(bitmap: Bitmap, maxSize: Int): ByteArray {
        var quality = 80
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos)
        var bytes = bos.toByteArray()
        if (bytes.size <= maxSize) return bytes
        while (bytes.size > maxSize && quality > 0) {
            quality -= 10
            val bos2 = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos2)
            bytes = bos2.toByteArray()
            if (bytes.size <= maxSize) break
        }
        return bytes
    }

    private fun downloadBitmap(url: String): Bitmap? = try {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.inputStream.use { BitmapFactory.decodeStream(it) }
    } catch (e: Exception) {
        Log.w(TAG, "无法加载缩略图: ${e.message}")
        null
    }
}