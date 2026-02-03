package com.ssafy.closetory.baseCode.data.local

import android.content.Context
import android.content.SharedPreferences
import com.ssafy.closetory.ApplicationClass

private const val TAG = "SharedPreferencesUtil_싸피"
class SharedPreferencesUtil(context: Context) {
    private var preferences: SharedPreferences =
        context.getSharedPreferences(ApplicationClass.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    // 쿠키 내용은
    // 현재 사용 안 하는 중
    fun addUserCookie(cookies: HashSet<String>) {
        val editor = preferences.edit()
        editor.putStringSet(ApplicationClass.COOKIES_KEY_NAME, cookies)
        editor.apply()
    }

    // 현재 사용 안 하는 중
    fun getUserCookie(): MutableSet<String>? = preferences.getStringSet(ApplicationClass.COOKIES_KEY_NAME, HashSet())

    fun putUserNickName(nickname: String, key: String = "NICKNAME") {
        preferences.edit().putString(key, nickname).apply()
    }

    fun getUserNickName(key: String = "NICKNAME", defaultValue: String = "NICK_NAME_NULL"): String? =
        preferences.getString(key, defaultValue)

    fun putUserId(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    fun getUserId(key: String, defaultValue: Int = -1): Int? = preferences.getInt(key, defaultValue)

    fun clearUserId(key: String) {
        preferences.edit().remove(key).apply()
    }

    // 온보딩 완료 플래그 추가
    private fun onboardingKey(userId: Int) = "onboarding_done_$userId"

    // 저장된 적 없으면 기본값 false로 동작
    fun isOnboardingDone(userId: Int): Boolean = preferences.getBoolean(onboardingKey(userId), false)

    // 온보딩 완료(true) / 필요하면 false로 되돌릴 수도 있음
    fun setOnboardingDone(userId: Int, done: Boolean) {
        preferences.edit()
            .putBoolean(onboardingKey(userId), done)
            .apply()
    }

    fun clearUserNickName(key: String = "NICKNAME") {
        preferences.edit().remove(key).apply()
    }

    // 지금 사용 안 하는 중
    fun getString(key: String, string: String): String? = preferences.getString(key, null)
}
