package com.ssafy.closetory.homeActivity.home

import android.util.Log
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.StylingResponse
import kotlin.math.log
import retrofit2.Response

private const val TAG = "HomeRepository_싸피"
class HomeRepository {
    private val service: HomeService = ApplicationClass.retrofit.create(HomeService::class.java)

    suspend fun getStylingList(isMain: Boolean): Response<ApiResponse<List<StylingResponse>>> =
        service.getStylingList(isMain)
}
