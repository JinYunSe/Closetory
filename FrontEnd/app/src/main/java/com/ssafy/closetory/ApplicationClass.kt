package com.ssafy.closetory

import android.app.Application
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ssafy.closetory.baseCode.data.local.SharedPreferencesUtil
import com.ssafy.closetory.util.AuthInterceptor
import com.ssafy.closetory.util.AuthManager
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class
ApplicationClass : Application() {

    companion object {

        val SERVER_URL = "http://i14d102.p.ssafy.io:8080/api/v1/"

        lateinit var sharedPreferences: SharedPreferencesUtil

        lateinit var authManager: AuthManager

        // JWT Token Header 키 값
        const val X_ACCESS_TOKEN = "Authorization"
        const val SHARED_PREFERENCES_NAME = "SSAFY_TEMPLATE_APP"
        const val COOKIES_KEY_NAME = "cookies"
        const val USERID = "userId"

        // 전역변수 문법을 통해 Retrofit 인스턴스를 앱 실행 시 1번만 생성하여 사용
        lateinit var retrofit: Retrofit
    }

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = SharedPreferencesUtil(this)

        authManager = AuthManager(this)

        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(5000, TimeUnit.MILLISECONDS)
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            // 자동으로 해더에 token 붙여 넣기
            .addInterceptor(AuthInterceptor())
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

    // GSon은 엄격한 json type을 요구하는데, 느슨하게 하기 위한 설정.
    // success, fail등 문자로 리턴될 경우 오류 발생한다. json 문자열이 아니라고..
    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
}
