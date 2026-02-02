// MyPageService

package com.ssafy.closetory.homeActivity.myPage

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.dto.EditProfilePasswordCheckRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MyPageService {

    // 현재 유저정보 불러오기
    @GET("users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: Int): Response<ApiResponse<EditProfileInfoResponse>>

    // 비밀번호 검증
    @POST("users/{userId}/password")
    suspend fun checkPassword(
        @Path("userId") userId: Int,
        @Body request: EditProfilePasswordCheckRequest
    ): Response<ApiResponse<Unit>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>
}
