// homeActivity/registrationCloth/RegistrationClothRepository.kt
package com.ssafy.closetory.homeActivity.registrationCloth

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.ClothesIdDto
import com.ssafy.closetory.dto.MaskedImageResponse
import com.ssafy.closetory.dto.RegistrationClothDto
import okhttp3.MultipartBody
import retrofit2.Response

class RegistrationClothRepository {

    private val service: RegistrationClothService =
        ApplicationClass.retrofit.create(RegistrationClothService::class.java)

    suspend fun removeImageBackground(clothesPhoto: MultipartBody.Part): Response<ApiResponse<MaskedImageResponse>> =
        service.removeImageBackground(clothesPhoto)

    suspend fun registrationCloth(req: RegistrationClothDto): Response<ApiResponse<ClothesIdDto>> =
        service.registrationCloth(req)

    suspend fun patchCloth(clothesId: Int, req: RegistrationClothDto): Response<ApiResponse<Unit>> =
        service.patchCloth(clothesId, req)
}
