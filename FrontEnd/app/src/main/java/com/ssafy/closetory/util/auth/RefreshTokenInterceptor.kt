package com.ssafy.closetory.util.auth

import com.ssafy.closetory.ApplicationClass
import okhttp3.Interceptor
import okhttp3.Response

class RefreshTokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val refresh = ApplicationClass.authManager.getRefreshToken()

        val request = chain.request().newBuilder().apply {
            if (!refresh.isNullOrBlank()) {
                header(ApplicationClass.X_REFRESH_TOKEN, refresh)
            }
        }.build()

        return chain.proceed(request)
    }
}
