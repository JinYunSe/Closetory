package com.ssafy.closetory.util.Auth

import com.ssafy.closetory.ApplicationClass
import okhttp3.Interceptor
import okhttp3.Response

class RefreshTokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val refresh = ApplicationClass.authManager.getRefreshToken()
        val userId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID)

        val request = chain.request().newBuilder().apply {
            if (!refresh.isNullOrBlank()) {
                header(ApplicationClass.X_REFRESH_TOKEN, refresh)
            }

            if (userId != -1) {
                header(ApplicationClass.USERID, userId.toString())
            }
        }.build()

        return chain.proceed(request)
    }
}
