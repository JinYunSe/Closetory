package com.ssafy.closetory.homeActivity.codyRepository

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.CodyRepositoryResponse
import retrofit2.Response
import retrofit2.http.GET

interface CodyRepositoryService {
    @GET("looks")
    suspend fun getlooks(): Response<ApiResponse<List<CodyRepositoryResponse>>>
}
