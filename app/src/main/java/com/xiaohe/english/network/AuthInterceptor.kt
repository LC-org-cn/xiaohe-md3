package com.xiaohe.english.network

import com.xiaohe.english.data.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val accessToken = tokenManager.getAccessToken()
        val builder = request.newBuilder()
        builder.addHeader(ApiConstants.HEADER_TENANT_ID, ApiConstants.TENANT_ID)
        if (!accessToken.isNullOrEmpty()) {
            builder.addHeader(ApiConstants.HEADER_AUTHORIZATION, accessToken)
        }
        if (request.header(ApiConstants.HEADER_CONTENT_TYPE) == null) {
            builder.addHeader(ApiConstants.HEADER_CONTENT_TYPE, ApiConstants.CONTENT_TYPE_JSON)
        }
        return chain.proceed(builder.build())
    }
}
