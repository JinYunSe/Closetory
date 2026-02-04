package com.ssafy.closetory.homeActivity.registrationClothes

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.ClothesIdDto
import com.ssafy.closetory.dto.PhotoUrlDto
import com.ssafy.closetory.dto.RegistrationClothesDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface RegistrationClothesService {

    // 배경 제거
    @Multipart
    @POST("clothes/masking")
    suspend fun removeImageBackground(@Part clothesPhoto: MultipartBody.Part): Response<ApiResponse<PhotoUrlDto>>

    // 옷 등록
    @POST("clothes")
    suspend fun registrationCloth(@Body req: RegistrationClothesDto): Response<ApiResponse<ClothesIdDto>>

    // 옷 수정
    @PATCH("clothes/{clothesId}")
    suspend fun patchCloth(
        @Path("clothesId") clothesId: Int,
        @Body req: RegistrationClothesDto
    ): Response<ApiResponse<Unit>>

    @POST("clothes/editing")
    suspend fun requestClothesAlteration(@Body req: PhotoUrlDto): Response<ApiResponse<PhotoUrlDto>>
}
