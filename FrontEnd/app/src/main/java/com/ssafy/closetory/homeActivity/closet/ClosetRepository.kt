package com.ssafy.closetory.homeActivity.closet

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.ClosetResponse
import retrofit2.Response

class ClosetRepository {

    private val service: ClosetService =
        ApplicationClass.retrofit.create(ClosetService::class.java)

    suspend fun getClothesList(
        tags: List<Int>?,
        color: String?,
        seasons: List<Int>?,
        onlyMine: Boolean?
    ): Response<ApiResponse<ClosetResponse>> = service.getClothesList(
        tags,
        color,
        seasons,
        onlyMine
    )
}
