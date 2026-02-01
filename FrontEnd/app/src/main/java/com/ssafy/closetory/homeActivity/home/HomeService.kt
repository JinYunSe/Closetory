package com.ssafy.closetory.homeActivity.home

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.StylingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeService {
    @GET("looks/monthly")
    suspend fun getStylingList(
        @Query("date") date: String,
        @Query("isMain") isMain: Boolean
    ): Response<ApiResponse<List<StylingResponse>>>
}
