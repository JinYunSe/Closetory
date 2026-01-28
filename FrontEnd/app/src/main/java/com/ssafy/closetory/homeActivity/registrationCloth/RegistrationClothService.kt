package com.ssafy.closetory.homeActivity.registrationCloth

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.ClothesIdDto
import com.ssafy.closetory.dto.MaskedImageResponse
import com.ssafy.closetory.dto.RegistrationClothDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface RegistrationClothService {

    // 배경 제거
    @Multipart
    @POST("/api/v1/clothes/mask")
    suspend fun removeImageBackground(
        @Part clothesPhoto: MultipartBody.Part
    ): Response<ApiResponse<MaskedImageResponse>>

    // 옷 등록
    @POST("/api/v1/clothes")
    suspend fun registrationCloth(@Body req: RegistrationClothDto): Response<ApiResponse<ClothesIdDto>>

    // 옷 수정
    @PATCH("/api/v1/clothes/{clothesId}")
    suspend fun patchCloth(
        @Path("clothesId") clothesId: Int,
        @Body req: RegistrationClothDto
    ): Response<ApiResponse<Unit>>
}
