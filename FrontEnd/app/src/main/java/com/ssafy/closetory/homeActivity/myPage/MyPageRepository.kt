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

        // 성공이면 그냥 body()
        res.body()?.let { return it }

        // 실패면 errorBody를 ApiResponse<Unit>로 파싱 시도
        val converter = ApplicationClass.retrofit
            .responseBodyConverter<ApiResponse<Unit>>(ApiResponse::class.java, emptyArray())

        val parsedError: ApiResponse<Unit>? = res.errorBody()?.let { body ->
            runCatching { converter.convert(body) }.getOrNull()
        }

        return parsedError ?: ApiResponse(
            httpStatusCode = res.code(),
            responseMessage = null,
            errorMessage = "비밀번호가 올바르지 않습니다.", // 파싱 실패시 fallback
            data = null
        )
    }

    suspend fun logout() = service.logout()

    suspend fun getTagsStatistics(userId: Int) = service.getTagsStatistics(userId)

    suspend fun getColorsStatistics(userId: Int) = service.getColorsStatistics(userId)

    suspend fun getTop3Clothes(userId: Int) = service.getTop3Clothes(userId)

    suspend fun getRecentCody() = service.getRecentCody()
}
