package com.ssafy.closetory.homeActivity.mypage

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.dto.EditProfilePasswordCheckRequest
import com.ssafy.closetory.dto.Top3ClothesResponse
import retrofit2.Response

private const val TAG = "MyPageRepository_싸피"

class MyPageRepository {

    private val service =
        ApplicationClass.retrofit.create(MyPageService::class.java)

    suspend fun getUserProfile(userId: Int): Response<ApiResponse<EditProfileInfoResponse>> =
        service.getUserProfile(userId)

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

    // ✅ 코디 히스토리(Top3 착용 옷) 추가
    suspend fun getTop3Clothes(userId: Int): Response<ApiResponse<List<Top3ClothesResponse>>> =
        service.getTop3Clothes(userId)
}
