package com.ssafy.closetory.homeActivity.aiStyling

import android.util.Log
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.AiCoordinationRequest
import com.ssafy.closetory.dto.AiCoordinationResponse
import com.ssafy.closetory.dto.AiFittingRequest
import com.ssafy.closetory.dto.AiFittingResponse
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.SaveLookRequest
import com.ssafy.closetory.homeActivity.styling.StylingRepository
import retrofit2.Response

class AiStylingRepository {

    private val aiStylingService = ApplicationClass.retrofit.create(AiStylingService::class.java)

    // 룩 저장/가상피팅 재사용
    private val stylingRepository = StylingRepository()

    suspend fun getAiRecommendation(
        isPersonalized: Boolean,
        onlyMine: Boolean
    ): Response<ApiResponse<AiCoordinationResponse>> {
        val request = AiCoordinationRequest(
            isPersonalized = isPersonalized,
            onlyMine = onlyMine
        )
        Log.d("AiStylingRepository", "AI 추천 요청: isPersonalized=$isPersonalized, onlyMine=$onlyMine")
        return aiStylingService.getAiRecommendation(request)
    }

    // 직접 코디에서 재사용
    suspend fun requestAiFitting(request: AiFittingRequest): Response<ApiResponse<AiFittingResponse>> =
        stylingRepository.requestAiFitting(request)

    suspend fun saveLook(request: SaveLookRequest): Response<ApiResponse<Unit>> = stylingRepository.saveLook(request)
}
