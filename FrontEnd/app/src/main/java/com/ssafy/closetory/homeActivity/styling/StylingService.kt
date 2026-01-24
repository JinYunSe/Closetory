package com.ssafy.closetory.homeActivity.styling

import com.ssafy.closetory.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface StylingService {

    /**
     * 룩 저장 (직접 코디)
     * POST /api/v1/looks
     */
    @POST("looks")
    suspend fun saveLook(@Body request: SaveLookRequest): Response<ApiResponse<SaveLookResponse>>
}

/**
 * 룩 저장 요청 데이터
 * 순서: Top, Bottom, Shoes, Outer, Accessory, Bag
 */
data class SaveLookRequest(val clothesIdList: List<Long>)

/**
 * 룩 저장 응답 데이터
 */
data class SaveLookResponse(
    val topCloth: String?,
    val bottomCloth: String?,
    val shoes: String,
    val outerCloth: String?,
    val accessories: String?,
    val bags: String?
)
