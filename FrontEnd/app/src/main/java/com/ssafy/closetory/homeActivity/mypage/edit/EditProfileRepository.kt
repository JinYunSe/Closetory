package com.ssafy.closetory.homeActivity.mypage.edit

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.EditProfileBaseResponse
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.dto.EditProfilePasswordRequest
import com.ssafy.closetory.dto.EditProfileUpdateRequest
import retrofit2.Response

class EditProfileRepository {

    private val editProfileService: EditProfileService =
        ApplicationClass.retrofit.create(EditProfileService::class.java)

    suspend fun getUserProfile(userId: Int): Response<EditProfileInfoResponse> = editProfileService.getUserProfile(
        userId = userId
    )

    suspend fun updateProfile(userId: Int, request: EditProfileUpdateRequest): Response<EditProfileBaseResponse> =
        editProfileService.updateProfile(
            userId = userId,
            request = request
        )

    suspend fun changePassword(request: EditProfilePasswordRequest): Response<EditProfileBaseResponse> =
        editProfileService.changePassword(
            request = request
        )
}
