package com.ssafy.closetory.homeActivity.home

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.StylingResponse
import retrofit2.Response

class HomeRepository {
    private val service: HomeService = ApplicationClass.retrofit.create(HomeService::class.java)

    suspend fun getStylingList(isMain: Boolean): Response<ApiResponse<List<StylingResponse>>> =
        service.getStylingList(isMain)
}
