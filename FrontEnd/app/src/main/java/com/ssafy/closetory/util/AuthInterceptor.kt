package com.ssafy.closetory.util

import android.util.Log
import com.ssafy.closetory.ApplicationClass
import okhttp3.Interceptor
import okhttp3.Response

// 모든 요청에 대해 해더에 token 넣기
private const val TAG = "AuthInterceptor_싸피"

// 요청에 Bearer 토큰 넣는 해더
class AuthInterceptor : okhttp3.Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val token = ApplicationClass.authManager.getAccessToken()

        // 토큰이 있는 상황이면
        val newRequest = if (!token.isNullOrEmpty()) {
            // 해더에 Bearer token 값 넣기
            original.newBuilder()
                .header(ApplicationClass.X_ACCESS_TOKEN, "Bearer $token")
                .build()
        } else {
            original
        }

        // log로 request할 때 토큰이 잘 가는지 확인하기
        Log.d(TAG, "url=${newRequest.url}")
        Log.d(TAG, "Authorization: ${newRequest.header("Authorization")}")

        return chain.proceed(newRequest)
    }
}
