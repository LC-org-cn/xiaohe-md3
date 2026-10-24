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
        // 任何子系统初始化失败都不允许拖垮整个应用（与原版顺序一致，但加防御）
        try {
            CrashLogUploader.initialize(this)
        } catch (t: Throwable) {
            Log.e("XiaoheApp", "CrashLogUploader init failed", t)
        }
        try {
            AudioLogManager.init(this)
        } catch (t: Throwable) {
            Log.e("XiaoheApp", "AudioLogManager init failed", t)
        }
        try {
            if (WeChatManager.init(this)) Log.d("XiaoheApp", "微信SDK初始化成功")
            else Log.w("XiaoheApp", "微信SDK初始化失败，分享功能可能不可用")
        } catch (t: Throwable) {
            Log.e("XiaoheApp", "WeChatManager init failed", t)
        }
        try {
            tokenManager = TokenManager(this)
            SessionStore.tokenManager = tokenManager
            ApiClient.init(tokenManager)
        } catch (t: Throwable) {
            Log.e("XiaoheApp", "network init failed", t)
            tokenManager = TokenManager(this)
            SessionStore.tokenManager = tokenManager
        }
    }
}