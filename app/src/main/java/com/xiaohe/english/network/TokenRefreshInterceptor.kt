package com.xiaohe.english.network

import com.xiaohe.english.data.TokenManager
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.net.URLEncoder

class TokenRefreshInterceptor(
    private val tokenManager: TokenManager,
    private val onUnauthorized: () -> Unit,
) : Interceptor {
    @Synchronized
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val bodyStr = response.peekBody(1024 * 256).string()
        val code = runCatching { JSONObject(bodyStr).optInt("code", -1) }.getOrDefault(-1)

        if (code == ApiConstants.CODE_TOKEN_EXPIRED) {
            val refreshToken = tokenManager.getRefreshToken()
            if (!refreshToken.isNullOrEmpty()) {
                val encoded = URLEncoder.encode(refreshToken, "UTF-8")
                val refreshReq = request.newBuilder()
                    .url(ApiConstants.BASE_URL + "/app-api/member/auth/refresh-token?refreshToken=" + encoded)
                    .method("POST", ByteArray(0).toRequestBody("application/json".toMediaType()))
                    .removeHeader(ApiConstants.HEADER_AUTHORIZATION)
                    .header(ApiConstants.HEADER_TENANT_ID, ApiConstants.TENANT_ID)
                    .header(ApiConstants.HEADER_CONTENT_TYPE, ApiConstants.CONTENT_TYPE_JSON)
                    .build()
                val refreshResp = chain.proceed(refreshReq)
                val refreshBody = refreshResp.peekBody(1024 * 256).string()
                val rCode = runCatching { JSONObject(refreshBody).optInt("code", -1) }.getOrDefault(-1)
                if (rCode == ApiConstants.CODE_SUCCESS) {
                    val data = JSONObject(refreshBody).optJSONObject("data")
                    if (data != null) {
                        tokenManager.saveToken(
                            com.xiaohe.english.network.model.TokenInfo(
                                accessToken = data.optString("accessToken"),
                                expiresTime = data.optLong("expiresTime"),
                                refreshToken = data.optString("refreshToken"),
                            )
                        )
                        val newReq = request.newBuilder()
                            .header(ApiConstants.HEADER_AUTHORIZATION, tokenManager.getAccessToken() ?: "")
                            .build()
                        return chain.proceed(newReq)
                    }
                } else {
                    tokenManager.clear()
                    onUnauthorized()
                }
            } else {
                tokenManager.clear()
                onUnauthorized()
            }
        }
        return response
    }
}
