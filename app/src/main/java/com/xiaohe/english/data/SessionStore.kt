package com.xiaohe.english.data

import com.xiaohe.english.network.model.TokenInfo

object SessionStore {
    @Volatile internal var tokenManager: TokenManager? = null

    fun save(token: TokenInfo) {
        tokenManager?.saveToken(token) ?: error("SessionStore not initialized")
    }

    fun clear() {
        tokenManager?.clear()
    }
}
