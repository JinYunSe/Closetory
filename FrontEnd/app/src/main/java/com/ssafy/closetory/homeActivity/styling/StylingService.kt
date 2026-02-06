package com.ssafy.closetory.homeActivity.styling

import com.ssafy.closetory.dto.AiFittingRequest
import com.ssafy.closetory.dto.AiFittingResponse
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.SaveLookRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface StylingService {
    // 룩 저장 (직접 코디)
    // POST /api/v1/looks
    @POST("looks")
    suspend fun saveLook(@Body request: SaveLookRequest): Response<ApiResponse<Unit>>

    @POST("looks/ai/fitting")
    suspend fun requestAiFitting(@Body request: AiFittingRequest): Response<ApiResponse<AiFittingResponse>>
}
