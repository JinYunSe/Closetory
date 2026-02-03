package com.ssafy.closetory.homeActivity.mypage.signout

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.SignoutRequest
import retrofit2.Response

class SignoutRepository {

    private val service: SignoutService =
        ApplicationClass.retrofit.create(SignoutService::class.java)

    suspend fun signout(userId: Int, password: String): Response<ApiResponse<Unit>> = service.signout(
        userId = userId,
        request = SignoutRequest(password)
    )
}
