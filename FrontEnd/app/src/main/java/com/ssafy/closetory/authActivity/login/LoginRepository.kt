// LoginRepository

package com.ssafy.closetory.authActivity.login

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.LoginRequest
import com.ssafy.closetory.dto.LoginResponse
import retrofit2.Response

class LoginRepository {

    private val loginService: LoginService =
        ApplicationClass.retrofit.create(LoginService::class.java)

    suspend fun login(request: LoginRequest): Response<ApiResponse<LoginResponse>> = loginService.login(request)
}
