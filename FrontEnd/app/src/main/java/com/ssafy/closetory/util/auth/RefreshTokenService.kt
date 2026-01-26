package com.ssafy.closetory.util.auth

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.TokenResponse
import retrofit2.Response
import retrofit2.http.POST
interface RefreshTokenService {
    @POST("auth/token")
    suspend fun refresh(): Response<ApiResponse<TokenResponse>>
}
