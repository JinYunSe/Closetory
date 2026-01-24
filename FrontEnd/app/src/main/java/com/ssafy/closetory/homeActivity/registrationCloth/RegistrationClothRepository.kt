package com.ssafy.closetory.homeActivity.registrationCloth

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.RegistrationClothRequest
import retrofit2.Response

class RegistrationClothRepository {

    private val service: RegistrationClothService =
        ApplicationClass.retrofit.create(RegistrationClothService::class.java)

    suspend fun registrationCloth(registrationClothRequest: RegistrationClothRequest): Response<ApiResponse<Unit>> =
        service.registrationCloth(
            registrationClothRequest
        )
}
