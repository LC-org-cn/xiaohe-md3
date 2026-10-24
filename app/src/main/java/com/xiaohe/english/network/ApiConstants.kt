package com.xiaohe.english.network

object ApiConstants {
    const val BASE_URL = "https://admin.shangheedu.com"
    const val CODE_SUCCESS = 0
    const val CODE_TOKEN_EXPIRED = 401
    const val CODE_UNAUTHORIZED = 401
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
    const val CONTENT_TYPE_JSON = "application/json"
    const val CONTENT_TYPE_FORM = "application/x-www-form-urlencoded"
    const val DEFAULT_PAGE_NO = 1
    const val DEFAULT_PAGE_SIZE = 10
    const val MAX_PAGE_SIZE = 100
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val HEADER_TENANT_ID = "tenant-id"
    const val TENANT_ID = "1"
    const val ENABLE_LOG = false
}
