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

    // 현재 사용 안 하는 중
    fun getString(key: String): String? = preferences.getString(key, null)

    fun putUserId(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    fun getUserId(key: String, defaultValue: Int = -1): Int? = preferences.getInt(key, defaultValue)

    fun clearUserId(key: String) {
        preferences.edit().remove(key).apply()
    }
}
