package com.ssafy.closetory.homeActivity.myPage.signout

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.SignoutRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.Path

// 회원 탈퇴 관련 API 정의
interface SignoutService {
    @HTTP(method = "DELETE", path = "users/{userId}", hasBody = true)
    suspend fun signout(@Path("userId") userId: Int, @Body request: SignoutRequest): Response<ApiResponse<Unit>>
}
