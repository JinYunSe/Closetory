// LogoutService

package com.ssafy.closetory.homeActivity.mypage

import com.ssafy.closetory.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.POST

interface MyPageService {

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>
}
