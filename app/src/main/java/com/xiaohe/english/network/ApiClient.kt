package com.xiaohe.english.network

import com.xiaohe.english.data.TokenManager
import com.xiaohe.english.network.api.*
import com.xiaohe.english.network.model.ApiException
import com.xiaohe.english.network.model.BaseResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    @Volatile private var instance: Retrofit? = null
    @Volatile private var tokenManagerRef: TokenManager? = null
    @Volatile var onUnauthorized: () -> Unit = {}

    fun init(tokenManager: TokenManager) {
        tokenManagerRef = tokenManager
        instance = build()
    }

    private fun build(): Retrofit {
        val tm = tokenManagerRef ?: error("ApiClient.init(tokenManager) must be called first")
        val client = OkHttpClient.Builder()
            .connectTimeout(ApiConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tm))
            .addInterceptor(TokenRefreshInterceptor(tm) { onUnauthorized() })
            .build()
        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL + "/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun retrofit(): Retrofit {
        if (instance == null) synchronized(this) {
            if (instance == null) instance = build()
        }
        return instance!!
    }

    val auth: AuthApiService get() = retrofit().create(AuthApiService::class.java)
    val user: UserApiService get() = retrofit().create(UserApiService::class.java)
    val study: StudyApiService get() = retrofit().create(StudyApiService::class.java)
    val work: WorkApiService get() = retrofit().create(WorkApiService::class.java)
    val common: CommonApiService get() = retrofit().create(CommonApiService::class.java)
    val material: MaterialApiService get() = retrofit().create(MaterialApiService::class.java)
    val order: OrderApiService get() = retrofit().create(OrderApiService::class.java)
    val buy: BuyApiService get() = retrofit().create(BuyApiService::class.java)
}

/** Unwrap BaseResponse: throw ApiException on non-zero code, else return data. */
suspend fun <T> unwrap(block: suspend () -> BaseResponse<T>): T? {
    val resp = block()
    if (resp.code != ApiConstants.CODE_SUCCESS) throw ApiException(resp.code, resp.msg ?: "请求失败")
    return resp.data
}
