package com.ssafy.closetory.homeActivity

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.TagResponse
import retrofit2.Response
import retrofit2.http.GET

interface HomeInitService {

    @GET("tags")
    suspend fun getTagsList(): Response<ApiResponse<List<TagResponse>>>
}
