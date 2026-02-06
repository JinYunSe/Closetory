package com.ssafy.closetory

import android.app.Application
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ssafy.closetory.baseCode.data.local.SharedPreferencesUtil
import com.ssafy.closetory.util.auth.AuthInterceptor
import com.ssafy.closetory.util.auth.AuthManager
import com.ssafy.closetory.util.auth.RefreshService
import com.ssafy.closetory.util.auth.TokenAuthenticator
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApplicationClass : Application() {

    companion object {
        // 댓글의 닉네임 사용
        const val NICKNAME = "nickname"

        lateinit var sharedPreferences: SharedPreferencesUtil

        lateinit var authManager: AuthManager

        val X_ACCESS_TOKEN: String get() = BuildConfig.X_ACCESS_TOKEN
        val X_REFRESH_TOKEN: String get() = BuildConfig.X_REFRESH_TOKEN
        val USERID: String get() = BuildConfig.USERID
        val SHARED_PREFERENCES_NAME: String get() = BuildConfig.SHARED_PREFERENCES_NAME
        val COOKIES_KEY_NAME: String get() = BuildConfig.COOKIES_KEY_NAME

        val API_BASE_URL: String
            get() = BuildConfig.BASE_URL.trim().removeSuffix("/") + "/"

        lateinit var retrofit: Retrofit
        lateinit var gson: Gson
    }

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = SharedPreferencesUtil(this)

        authManager = AuthManager(this)
        gson = GsonBuilder().setLenient().create()

        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(60000, TimeUnit.MILLISECONDS)
            .connectTimeout(1800000, TimeUnit.MILLISECONDS)
            // 자동으로 헤더에 token을 붙여 주기
            .addInterceptor(AuthInterceptor())
            .authenticator(TokenAuthenticator(RefreshService.api))
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build()

        // 앱 시작 시 Retrofit 인스턴스를 생성
        retrofit = Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
