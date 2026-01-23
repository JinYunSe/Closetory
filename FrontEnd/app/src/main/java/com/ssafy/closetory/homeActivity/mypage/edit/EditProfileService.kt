// EditProfileService

package com.ssafy.closetory.homeActivity.mypage.edit

import com.ssafy.closetory.dto.EditProfileBaseResponse
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.dto.EditProfilePasswordRequest
import com.ssafy.closetory.dto.EditProfileUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface EditProfileService {

    @GET("users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: Int): Response<EditProfileInfoResponse>

    @PATCH("users/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: Int,
        @Body request: EditProfileUpdateRequest
    ): Response<EditProfileBaseResponse>

    @PATCH("users/password")
    suspend fun changePassword(@Body request: EditProfilePasswordRequest): Response<EditProfileBaseResponse>
}
