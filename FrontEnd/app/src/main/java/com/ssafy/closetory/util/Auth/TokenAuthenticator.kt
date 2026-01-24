package com.ssafy.closetory.util.Auth

import android.util.Log
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.util.Auth.RefreshTokenService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

private const val TAG = "TokenAuthenticator_싸피"

// HttpStatusCode 401이 발생할 경우(토큰 갱신 필요할 때) 동작
class TokenAuthenticator(private val refreshTokenService: RefreshTokenService) : Authenticator {

    companion object {
        // 이미 작업 중인 스레드가 있는 경우
        // 다른 스레드가 synchronized 부분의 코드에 접근 못 하도록 막는 역할
        // => 한 번에 한 스레드만 접근 가능
        private val lock = Any()
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // 토큰 갱신 무한 반복 현상 방지를 위한 코드
        if (responseCount(response) >= 2) return null

        // refresh 토큰 자체도 만료된 상태이면 로그아웃 처리
        if (response.request.url.encodedPath.contains("/auth/refresh")) {
            ApplicationClass.authManager.clearToken()
            return null
        }

        // 다른 스레드가 이미 토큰 갱신 했는지 확인
        // 401이 동시에 여러 개 터질 경우 방지
        synchronized(lock) {
            // 이미 다른 요청이 갱신 했는지 확인
            val currentAccess = ApplicationClass.authManager.getAccessToken()

            // 기존 Bearer 토큰값 제거
            val requestAccess = response.request.header(ApplicationClass.X_ACCESS_TOKEN)
                ?.removePrefix("Bearer ")?.trim()

            // 현재 토큰이 존재하고 이전 토큰과 다른 경우
            // 즉, 갱신된 토큰인 경우 다시 원래 요청 보내기
            if (!currentAccess.isNullOrBlank() && currentAccess != requestAccess) {
                return response.request.newBuilder()
                    .header(ApplicationClass.X_ACCESS_TOKEN, "Bearer $currentAccess").build()
            }

            val refresh = ApplicationClass.authManager.getRefreshToken()

            if (refresh.isNullOrBlank()) return null

            // refresh 호출 => RefreshTokeninterceptor가 refreshToken을 Authorization에 넣음
            val refreshRes = try {
                runBlocking {
                    refreshTokenService.refresh()
                }
            } catch (_: Exception) {
                return null
            }

            // 통신 오류
            if (!refreshRes.isSuccessful) {
                Log.e(TAG, "서버 통신 실패")
                ApplicationClass.authManager.clearToken()
                return null
            }

            // 갱신된 토큰 변수에 담기
            val body = refreshRes.body()
            val tokenData = body?.data
            val newAccess = tokenData?.accessToken
            val newRefresh = tokenData?.refreshToken

            // 갱신 받은 토큰이 없는 경우
            if (newAccess.isNullOrBlank() || newRefresh.isNullOrBlank()) {
                Log.e(TAG, "서버에서 갱신된 토큰을 제공하지 않았습니다.")
                ApplicationClass.authManager.clearToken()
                return null
            }

            // accessToken과 refreshToken 저장
            ApplicationClass.authManager.saveTokens(newAccess, newRefresh)

            // HttpStatusCode : 401 오류가 난 호출 다시 요청
            return response.request.newBuilder()
                .header("Authorization", "Bearer $newAccess")
                .build()
        }
    }

    // 401 -> refresh -> 401 -> refresh ... 무한 반복 방지를 위한 코드
    private fun responseCount(response: Response): Int {
        var count = 1;
        var priority = response.priorResponse
        while (priority != null) {
            count++
            priority = priority.priorResponse
        }
        return count
    }
}
