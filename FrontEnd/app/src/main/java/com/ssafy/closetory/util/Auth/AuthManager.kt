package com.ssafy.closetory.util.Auth

import android.content.Context
import android.util.Log
import androidx.core.content.edit

private const val TAG = "AuthManager_싸피"

// Access, Refreash 토큰 매니저
class AuthManager(context: Context) {

    // 토큰 키를 호출해 사용하는 경우가 생길거라 companion object로 사용
    // + key : value 형식은 key 에서 오타가 나면 value를 가져오지 못 하는 상황 발생
    companion object {
        private const val PREFS_NAME = "auth_tokens"
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // accessToken과 refreshToken을 저장, 삭제 메서드를 하나로 합친 이유
    // 로그인, 로그아웃, 토큰 재발급 중 앱이 죽는 경우
    // 따로 메서드가 떨어져 있으면 상태 불일치가 생길 수 있다 하네요
    fun saveTokens(accessToken: String, refreshToken: String) {
        Log.d(TAG, "AuthManager SaveTokens: accessToken : $accessToken, refreshToken : $refreshToken")
        prefs.edit {
            putString(KEY_ACCESS, accessToken)
            putString(KEY_REFRESH, refreshToken)
        }
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH, null)

    fun clearToken() {
        prefs.edit {
            remove(KEY_ACCESS)
            remove(KEY_REFRESH)
        }
    }
}
