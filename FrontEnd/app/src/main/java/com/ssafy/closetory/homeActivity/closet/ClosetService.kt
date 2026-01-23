package com.ssafy.closetory.homeActivity.closet

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.ClosetResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ClosetService {

    @GET("clothes")
    suspend fun getClothesList(
        @Query("tags") tags: List<Int>?,
        @Query("color") color: String?,
        @Query("seasons") seasons: List<Int>?,
        @Query("onlyMine") onlyMine: Boolean?
    ): Response<ApiResponse<ClosetResponse>>
}
