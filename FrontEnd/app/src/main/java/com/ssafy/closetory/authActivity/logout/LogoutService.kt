// LogoutService

package com.ssafy.closetory.authActivity.logout

import com.ssafy.closetory.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.POST

interface LogoutService {

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>
}
