package com.ssafy.closetory.homeActivity.registrationCloth

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import retrofit2.Response

class RegistrationClothRepository {

    private val service: RegistrationClothService =
        ApplicationClass.retrofit.create(RegistrationClothService::class.java)

    suspend fun registrationCloth(
        photoUrl: String,
        tags: List<Int>,
        clothesTypes: String,
        seasons: List<Int>,
        color: String
    ): Response<ApiResponse<Unit>> = service.registrationCloth(
        photoUrl,
        tags,
        clothesTypes,
        seasons,
        color
    )
}
