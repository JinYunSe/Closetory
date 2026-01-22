package com.ssafy.closetory.util

import android.content.Context
import android.util.Base64
import android.util.Log
import org.json.JSONObject

private const val TAG = "AuthManager_싸피"
class AuthManager(context: Context) {
    private val prefs = context.getSharedPreferences("auth_access", Context.MODE_PRIVATE)

    fun saveAccessToken(token: String) {
        prefs.edit().putString("access_token", token).apply()
    }

    fun getAccessToken(): String? = prefs.getString("access_token", null)

    private val refresh = context.getSharedPreferences("auth_refresh", Context.MODE_PRIVATE)

    fun saveRefreshToken(token: String) {
        refresh.edit().putString("refresh_token", token).apply()
    }

    fun getRefreshToken(): String? = refresh.getString("refresh_token", null)

    fun clearToken() {
        prefs.edit().remove("access_token").apply()
        refresh.edit().remove("refresh_token").apply()
    }
    fun getUserIdFromToken(): String? {
        val token = getAccessToken() ?: return null

        Log.d(TAG, "getUserIdFromToken: $token")
        val parts = token.split(".")
        if (parts.size < 2) return null

        var payload = parts[1]

        // 🔥 Base64 padding 보정
        val padLength = 4 - (payload.length % 4)
        if (padLength in 1..3) {
            payload += "=".repeat(padLength)
        }

        return try {
            val decodedBytes = Base64.decode(
                payload,
                Base64.URL_SAFE or Base64.NO_WRAP
            )

            val json = JSONObject(String(decodedBytes, Charsets.UTF_8))
            Log.d(TAG, "getUserIdFromToken_json: $json")
            json.getString("sub")
        } catch (e: Exception) {
            null
        }
    }
}
