package com.ssafy.closetory.homeActivity.tagOnboarding

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.TagOnboardingRequest
import com.ssafy.closetory.dto.TagResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TagOnboardingService {
    // 태그 목록 불러오기
    @GET("tags")
    suspend fun getTagsList(): Response<ApiResponse<List<TagResponse>>>

    @POST("users/{userId}/myStyles")
    suspend fun postTagOnboarding(
        @Path("userId") userId: Int,
        @Body request: TagOnboardingRequest
    ): Response<ApiResponse<Unit>>
}
