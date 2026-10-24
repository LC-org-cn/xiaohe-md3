package com.xiaohe.english

import android.app.Application
import com.xiaohe.english.data.SessionStore
import com.xiaohe.english.data.TokenManager
import com.xiaohe.english.network.ApiClient

class XiaoheApp : Application() {
    lateinit var tokenManager: TokenManager
        private set

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(this)
        SessionStore.tokenManager = tokenManager
        ApiClient.init(tokenManager)
    }
}
