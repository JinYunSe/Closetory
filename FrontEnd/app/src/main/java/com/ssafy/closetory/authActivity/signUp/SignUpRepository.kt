// SignUpRepository.kt

package com.ssafy.closetory.authActivity.signUp

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.SignUpRequest
import retrofit2.Response

class SignUpRepository {

    private val signUpService: SignUpService =
        ApplicationClass.retrofit.create(SignUpService::class.java)

    suspend fun signUp(request: SignUpRequest): Response<ApiResponse<Unit>> = signUpService.signUp(request)
}
