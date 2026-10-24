package com.xiaohe.english.data

import android.content.Context
import android.content.SharedPreferences
import com.xiaohe.english.network.model.TokenInfo

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("xiaohe_secure_prefs", Context.MODE_PRIVATE)

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    fun getExpiresTime(): Long = prefs.getLong(KEY_EXPIRES_TIME, 0L)
    fun isLoggedIn(): Boolean = !getAccessToken().isNullOrBlank()

    @Synchronized
    fun saveToken(token: TokenInfo) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token.accessToken)
            .putString(KEY_REFRESH_TOKEN, token.refreshToken)
            .putLong(KEY_EXPIRES_TIME, token.expiresTime)
            .apply()
    }

    @Synchronized
    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_TIME = "expires_time"
    }
}
