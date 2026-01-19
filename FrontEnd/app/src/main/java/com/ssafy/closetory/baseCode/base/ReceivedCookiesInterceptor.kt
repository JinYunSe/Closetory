package com.ssafy.closetory.baseCode.base

import android.util.Log
import com.ssafy.closetory.ApplicationClass.Companion.sharedPreferences
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

private const val TAG = "ReceivedCookies_싸피"
class ReceivedCookiesInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse: Response = chain.proceed(chain.request())

        if (originalResponse.headers("Set-Cookie").isNotEmpty()) {
            val cookies = HashSet<String>()
            for (cookie in originalResponse.headers("Set-Cookie")) {
                cookies.add(cookie)
                Log.d(TAG, "intercept: $cookie")
            }

            // cookie 내부 데이터에 저장
            sharedPreferences.addUserCookie(cookies)
        }
        return originalResponse
    }
}
