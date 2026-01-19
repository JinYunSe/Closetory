package com.ssafy.closetory.baseCode.base

import com.ssafy.closetory.ApplicationClass.Companion.X_ACCESS_TOKEN
import com.ssafy.closetory.ApplicationClass.Companion.sharedPreferences
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class XAccessTokenInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()
        val jwtToken: String? = sharedPreferences.getString(X_ACCESS_TOKEN)
        if (jwtToken != null) {
            builder.addHeader("X-ACCESS-TOKEN", jwtToken)
        }
        return chain.proceed(builder.build())
    }
}
