// homeActivity/registrationCloth/RegistrationClothRepository.kt
package com.ssafy.closetory.homeActivity.registrationClothes

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.ClothesIdDto
import com.ssafy.closetory.dto.MaskedImageResponse
import com.ssafy.closetory.dto.RegistrationClothesDto
import okhttp3.MultipartBody
import retrofit2.Response

class RegistrationClothesRepository {

    private val service: RegistrationClothesService =
        ApplicationClass.retrofit.create(RegistrationClothesService::class.java)

    suspend fun removeImageBackground(clothesPhoto: MultipartBody.Part): Response<ApiResponse<MaskedImageResponse>> =
        service.removeImageBackground(clothesPhoto)

    suspend fun registrationCloth(req: RegistrationClothesDto): Response<ApiResponse<ClothesIdDto>> =
        service.registrationCloth(req)

    suspend fun patchCloth(clothesId: Int, req: RegistrationClothesDto): Response<ApiResponse<Unit>> =
        service.patchCloth(clothesId, req)
}
