package com.ssafy.closetory.homeActivity.closet

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.ClosetDataDto
import retrofit2.Response

class ClosetRepository {

    private val service: ClosetService =
        ApplicationClass.retrofit.create(ClosetService::class.java)

    suspend fun getClothesList(
        tags: List<String>?,
        color: String?,
        seasons: List<String>?,
        onlyLike: Boolean?,
        onlyMine: Boolean?
    ): Response<ApiResponse<ClosetDataDto>> = service.getClothesList(
        tags,
        color,
        seasons,
        onlyLike,
        onlyMine
    )
}
