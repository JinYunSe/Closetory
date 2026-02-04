package com.ssafy.closetory.homeActivity.mypage

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.dto.EditProfilePasswordCheckRequest
import retrofit2.Response

private const val TAG = "MyPageRepository_싸피"

class MyPageRepository {

    private val service =
        ApplicationClass.retrofit.create(MyPageService::class.java)

    // 현재 유저 정보 조회
    suspend fun getUserProfile(userId: Int): Response<ApiResponse<EditProfileInfoResponse>> =
        service.getUserProfile(userId)

    // 비밀번호 검증
    suspend fun checkPassword(userId: Int, password: String): ApiResponse<Unit> {
        val res = service.checkPassword(
            userId,
            EditProfilePasswordCheckRequest(password)
        )

        return if (res.isSuccessful && res.body() != null) {
            res.body()!!
        } else {
            ApiResponse(
                httpStatusCode = res.code(),
                responseMessage = null,
                errorMessage = res.errorBody()?.string(),
                data = null
            )
        }
    }

    suspend fun logout() = service.logout()

    suspend fun getTagsStatistics(userId: Int) = service.getTagsStatistics(userId)

    suspend fun getColorsStatistics(userId: Int) = service.getColorsStatistics(userId)

    suspend fun getTop3Clothes(userId: Int) = service.getTop3Clothes(userId)
}
