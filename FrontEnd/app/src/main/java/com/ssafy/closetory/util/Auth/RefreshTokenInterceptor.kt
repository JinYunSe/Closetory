package com.ssafy.closetory.util.Auth

import com.ssafy.closetory.ApplicationClass
import okhttp3.Interceptor
import okhttp3.Response

class RefreshTokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val refresh = ApplicationClass.authManager.getRefreshToken()

        val request = chain.request().newBuilder().apply {
            if (!refresh.isNullOrBlank()) {
                header(ApplicationClass.X_ACCESS_TOKEN, "Bearer $refresh")
            }
        }.build()

        return chain.proceed(request)
    }
}
