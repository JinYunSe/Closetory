package com.ssafy.closetory.homeActivity.registrationCloth

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.RegistrationClothRequest
import retrofit2.Response
import retrofit2.http.POST

interface RegistrationClothService {

    @POST("clothes")
    suspend fun registrationCloth(registrationClothRequest: RegistrationClothRequest): Response<ApiResponse<Unit>>
}
