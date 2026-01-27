package com.ssafy.closetory.homeActivity.registrationCloth

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.MaskedImageResponse
import com.ssafy.closetory.dto.OriginalImageRequest
import com.ssafy.closetory.dto.RegistrationClothRequest
import okhttp3.MultipartBody
import retrofit2.Response

class RegistrationClothRepository {

    private val service: RegistrationClothService =
        ApplicationClass.retrofit.create(RegistrationClothService::class.java)

    suspend fun registrationCloth(registrationClothRequest: RegistrationClothRequest): Response<ApiResponse<Unit>> =
        service.registrationCloth(
            registrationClothRequest
        )

    suspend fun removeImageBackground(
        originalImageRequest: MultipartBody.Part
    ): Response<ApiResponse<MaskedImageResponse>> = service.removeImageBackground(originalImageRequest)
}
