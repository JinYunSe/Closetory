package com.ssafy.closetory.homeActivity.registrationCloth

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.MaskedImageResponse
import com.ssafy.closetory.dto.OriginalImageRequest
import com.ssafy.closetory.dto.RegistrationClothRequest
import retrofit2.Response
import retrofit2.http.POST

interface RegistrationClothService {

    @POST("clothes")
    suspend fun registrationCloth(registrationClothRequest: RegistrationClothRequest): Response<ApiResponse<Unit>>

    @POST("masking")
    suspend fun removeImageBackground(
        originalImageRequest: OriginalImageRequest
    ): Response<ApiResponse<MaskedImageResponse>>
}
