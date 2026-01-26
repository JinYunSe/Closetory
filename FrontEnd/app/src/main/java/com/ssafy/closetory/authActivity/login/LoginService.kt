// LoginService
package com.ssafy.closetory.authActivity.login

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.LoginRequest
import com.ssafy.closetory.dto.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<TokenResponse>>
}
