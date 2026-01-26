package com.ssafy.closetory.homeActivity.mypage

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.EditProfilePasswordCheckRequest

private const val TAG = "MyPageRepository_싸피"

class MyPageRepository {

    private val service: MyPageService =
        ApplicationClass.retrofit.create(MyPageService::class.java)

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
}
