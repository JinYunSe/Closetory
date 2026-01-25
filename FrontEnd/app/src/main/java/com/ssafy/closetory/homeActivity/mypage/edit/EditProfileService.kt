// EditProfileService

package com.ssafy.closetory.homeActivity.mypage.edit

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.dto.EditProfilePasswordRequest
import com.ssafy.closetory.dto.EditProfileUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface EditProfileService {

    // 현재 유저정보 불러오기
    @GET("users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: Int): Response<ApiResponse<EditProfileInfoResponse>>

    // 현재 유저정보 변경하기
    @PATCH("users/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: Int,
        @Body request: EditProfileUpdateRequest
    ): Response<ApiResponse<Unit>>

    // 현재 비밀번호 변경하기
    @PATCH("users/{userId}/password")
    suspend fun changePassword(
        @Path("userId") userId: Int,
        @Body request: EditProfilePasswordRequest
    ): Response<ApiResponse<Unit>>
}
