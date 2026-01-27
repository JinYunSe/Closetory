// RegistrationClothService.kt
package com.ssafy.closetory.homeActivity.registrationCloth

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.MaskedImageResponse
import com.ssafy.closetory.dto.RegistrationClothRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RegistrationClothService {

    // 옷 등록
    @POST("clothes")
    suspend fun registrationCloth(registrationClothRequest: RegistrationClothRequest): Response<ApiResponse<Unit>>

    // 배경 제거
    @Multipart
    @POST("clothes/masking")
    suspend fun removeImageBackground(photo: MultipartBody.Part): Response<ApiResponse<MaskedImageResponse>>
}
