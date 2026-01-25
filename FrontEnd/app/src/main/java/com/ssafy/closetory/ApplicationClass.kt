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

        val SERVER_URL = "http://i14d102.p.ssafy.io:8080/api/v1/"

        lateinit var sharedPreferences: SharedPreferencesUtil

        lateinit var authManager: AuthManager

        // JWT Token Header 키 값
        const val X_ACCESS_TOKEN = "Authorization"

        const val X_REFRESH_TOKEN = "X_REFRESH_TOKEN"

        const val USERID = "userId"

        const val SHARED_PREFERENCES_NAME = "SSAFY_CLOSETORY"
        const val COOKIES_KEY_NAME = "cookies"

        // 전역변수 문법을 통해 Retrofit 인스턴스를 앱 실행 시 1번만 생성하여 사용
        lateinit var retrofit: Retrofit

        lateinit var gson: Gson
    }

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = SharedPreferencesUtil(this)

        authManager = AuthManager(this)

        // GSon은 엄격한 json type을 요구하는데, 느슨하게 하기 위한 설정.
        gson = GsonBuilder().setLenient().create()

        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(5000, TimeUnit.MILLISECONDS)
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            // 자동으로 해더에 accessToken 붙여 넣기
            .addInterceptor(AuthInterceptor())
            // HttpStatusCode 401의 경우 자동으로 토큰 갱신
            .authenticator(TokenAuthenticator(RefreshService.api))
            // 로그캣에 okhttp.OkHttpClient로 검색하면 http 통신 내용을 보여줍니다.
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build()

        // 앱이 처음 생성되는 순간, retrofit 인스턴스를 생성
        retrofit = Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
