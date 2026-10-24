package com.xiaohe.english

import android.app.Application
import android.util.Log
import com.xiaohe.english.data.SessionStore
import com.xiaohe.english.data.TokenManager
import com.xiaohe.english.network.ApiClient
import com.xiaohe.english.utils.AudioLogManager
import com.xiaohe.english.utils.CrashLogUploader
import com.xiaohe.english.wechat.WeChatManager

class XiaoheApp : Application() {
    lateinit var tokenManager: TokenManager
        private set

    override fun onCreate() {
        super.onCreate()
        // 与原版 XiaoheEnglishApp.onCreate 初始化顺序一致
        CrashLogUploader.initialize(this)
        Log.d("XiaoheApp", "崩溃日志收集器已初始化")
        AudioLogManager.init(this)
        if (WeChatManager.init(this)) {
            Log.d("XiaoheApp", "微信SDK初始化成功")
        } else {
            Log.w("XiaoheApp", "微信SDK初始化失败，分享功能可能不可用")
        }
        tokenManager = TokenManager(this)
        SessionStore.tokenManager = tokenManager
        ApiClient.init(tokenManager)
    }
}