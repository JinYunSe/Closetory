// SignUpService.kt

package com.ssafy.closetory.authActivity.signUp

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.SignUpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SignUpService {

    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<ApiResponse<Unit>>
}
