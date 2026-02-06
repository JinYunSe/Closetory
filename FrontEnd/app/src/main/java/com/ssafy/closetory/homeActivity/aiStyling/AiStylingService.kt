package com.ssafy.closetory.homeActivity.aiStyling

import com.ssafy.closetory.dto.AiCoordinationRequest
import com.ssafy.closetory.dto.AiCoordinationResponse
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.SaveLookRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AiStylingService {
    // AI 추천 룩 생성
    @POST("looks/ai/recommendation")
    suspend fun getAiRecommendation(@Body request: AiCoordinationRequest): Response<ApiResponse<AiCoordinationResponse>>
}
