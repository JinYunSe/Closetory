// LogoutService

package com.ssafy.closetory.authActivity.logout

import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.POST

interface LogoutService {

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") accessToken: String): Response<Unit>
}
