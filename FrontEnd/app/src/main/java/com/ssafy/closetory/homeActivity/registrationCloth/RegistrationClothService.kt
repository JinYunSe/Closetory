package com.ssafy.closetory.homeActivity.registrationCloth

import com.ssafy.closetory.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.POST

interface RegistrationClothService {

    @POST("clothes")
    suspend fun registrationCloth(
        photoUrl: String,
        tags: List<Int>,
        clothesTypes: String,
        seasons: List<Int>,
        color: String
    ): Response<ApiResponse<Unit>>
}
