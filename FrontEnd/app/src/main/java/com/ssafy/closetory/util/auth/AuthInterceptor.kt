package com.ssafy.closetory.util.auth

import android.util.Log
import com.ssafy.closetory.ApplicationClass
import okhttp3.Interceptor
import okhttp3.Response

// 모든 요청에 대해 해더에 token 넣기
private const val TAG = "AuthInterceptor_싸피"

// 요청에 Bearer 토큰 넣는 해더
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // 토큰 필요 없는 주소
        val noAuth = listOf("/auth/signup", "/auth/login", "/auth/token")

        // 로그 아웃은 refreshToken를 서버가 요구해서 예외로 refreshToken을 제공
        if (path.contains("/auth/logout")) {
            val refreshToken = ApplicationClass.authManager.getRefreshToken()

            val newReq = if (!refreshToken.isNullOrBlank()) {
                request.newBuilder()
                    .header(ApplicationClass.X_REFRESH_TOKEN, refreshToken)
                    .build()
            } else {
                request
            }

            Log.d(TAG, "url=${newReq.url}")
            Log.d(TAG, "${ApplicationClass.X_REFRESH_TOKEN}: ${newReq.header(ApplicationClass.X_REFRESH_TOKEN)}")

            return chain.proceed(newReq)
        }

        // 토큰이 필요 없는 기능은 기능 진행시키기
        if (noAuth.any { path.contains(it) }) return chain.proceed(request)

        val accessToken = ApplicationClass.authManager.getAccessToken()

        // 토큰이 있는 상황이면
        val newRequest = if (!accessToken.isNullOrEmpty()) {
            // 해더에 Bearer token 값 넣기
            request.newBuilder()
                .header(ApplicationClass.X_ACCESS_TOKEN, "Bearer $accessToken")
                .build()
        } else {
            request
        }

        // log로 request할 때 토큰이 잘 가는지 확인하기
        Log.d(TAG, "url=${newRequest.url}")
        Log.d(TAG, "Authorization: ${newRequest.header("Authorization")}")

        return chain.proceed(newRequest)
    }
}
