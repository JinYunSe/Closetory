package com.ssafy.closetory.homeActivity.mypage

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.EditProfilePasswordCheckRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface MyPageService {

    // 비밀번호 검증
    @POST("user/{userId}/password")
    suspend fun checkPassword(
        @Path("userId") userId: Int,
        @Body request: EditProfilePasswordCheckRequest
    ): Response<ApiResponse<Unit>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>
}
