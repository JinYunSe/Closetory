package com.ssafy.closetory.homeActivity.styling

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.ClosetResponse
import com.ssafy.closetory.homeActivity.closet.ClosetService
import retrofit2.Response

class StylingRepository {

    // ClosetService 재사용 (옷 조회용)
    private val closetService: ClosetService =
        ApplicationClass.retrofit.create(ClosetService::class.java)

    // StylingService (룩 저장용)
    private val stylingService: StylingService =
        ApplicationClass.retrofit.create(StylingService::class.java)

    /**
     * 의류 리스트 조회 (ClosetService 재사용)
     */
    suspend fun getClothesList(
        tags: List<Int>?,
        color: String?,
        seasons: List<Int>?,
        onlyMine: Boolean?
    ): Response<ApiResponse<ClosetResponse>> = closetService.getClothesList(
        tags,
        color,
        seasons,
        onlyMine
    )

    /**
     * 룩 저장
     */
    suspend fun saveLook(request: SaveLookRequest): Response<ApiResponse<SaveLookResponse>> =
        stylingService.saveLook(request)
}
