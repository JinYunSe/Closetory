package com.ssafy.closetory.baseCode.data.local

import android.content.Context
import android.content.SharedPreferences
import com.ssafy.closetory.ApplicationClass

class SharedPreferencesUtil(context: Context) {
    private var preferences: SharedPreferences =
        context.getSharedPreferences(ApplicationClass.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun addUserCookie(cookies: HashSet<String>) {
        val editor = preferences.edit()
        editor.putStringSet(ApplicationClass.COOKIES_KEY_NAME, cookies)
        editor.apply()
    }

    fun getUserCookie(): MutableSet<String>? = preferences.getStringSet(ApplicationClass.COOKIES_KEY_NAME, HashSet())

    fun getString(key: String): String? = preferences.getString(key, null)

    fun putUserId(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    fun getUserId(key: String, defaultValue: Int = -1): Int? = preferences.getInt(key, defaultValue)
}
