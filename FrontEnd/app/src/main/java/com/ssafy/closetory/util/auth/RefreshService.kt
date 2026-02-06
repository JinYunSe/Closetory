package com.ssafy.closetory.util.auth

import com.ssafy.closetory.ApplicationClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RefreshService {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(RefreshTokenInterceptor())
            .build()
    }

    val api: RefreshTokenService by lazy {
        Retrofit.Builder()
            .baseUrl(ApplicationClass.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(ApplicationClass.gson))
            .build()
            .create(RefreshTokenService::class.java)
    }
}
