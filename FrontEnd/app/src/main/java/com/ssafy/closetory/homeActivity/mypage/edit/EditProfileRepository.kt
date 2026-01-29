// EditProfileRepository

package com.ssafy.closetory.homeActivity.mypage.edit

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.dto.EditProfilePasswordRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

private const val TAG = "EditProfileRepository_싸피"

// 회원정보 관련 API 호출을 담당하는 Repository
class EditProfileRepository {

    private val service: EditProfileService =
        ApplicationClass.retrofit.create(EditProfileService::class.java)

    // 현재 유저 정보 조회
    suspend fun getUserProfile(userId: Int): Response<ApiResponse<EditProfileInfoResponse>> =
        service.getUserProfile(userId)

    // 회원정보 수정
    suspend fun updateProfileMultipart(
        userId: Int,
        profilePhoto: MultipartBody.Part?,
        bodyPhoto: MultipartBody.Part?,
        data: RequestBody
    ): Response<ApiResponse<Unit>> = service.updateProfileMultipart(
        userId = userId,
        profilePhoto = profilePhoto,
        bodyPhoto = bodyPhoto,
        data = data
    )

    // 비밀번호 변경
    suspend fun changePassword(userId: Int, request: EditProfilePasswordRequest): Response<ApiResponse<Unit>> =
        service.changePassword(
            userId = userId,
            request = request
        )
}
