package com.ssafy.closetory.homeActivity.tagOnboarding

import com.ssafy.closetory.dto.TagOnboardingRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface TagOnboardingService {
    @POST("users/{userId}/myStyles")
    suspend fun postTagOnboarding(
        @Path("userId") userId: Int,
        @Body request: TagOnboardingRequest
    ): retrofit2.Response<com.ssafy.closetory.dto.ApiResponse<Unit>>
}
