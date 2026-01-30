package com.ssafy.closetory.homeActivity.closet

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.ClosetResponse
import com.ssafy.closetory.dto.ClothesItemDto
import retrofit2.Response

class ClosetRepository {

    private val service: ClosetService =
        ApplicationClass.retrofit.create(ClosetService::class.java)

    suspend fun getRecommendedClothes(clothesId: Int): Response<ApiResponse<List<ClothesItemDto>>> =
        service.getRecommendedClothes(clothesId)

    suspend fun getClothesList(
        tags: List<Int>?,
        color: String?,
        seasons: List<Int>?,
        onlyMine: Boolean?
    ): Response<ApiResponse<ClosetResponse>> = service.getClothesList(
        tags,
        color,
        seasons,
        onlyMine
    )

    suspend fun getClothesDetail(clothesId: Int): Response<ApiResponse<ClothesItemDto>> = service.getClothesDetail(
        clothesId
    )

    suspend fun deleteClothes(clothesId: Int): Response<ApiResponse<Unit>> = service.deleteClothes(clothesId)

    suspend fun deleteClothesRental(clothesId: Int): Response<ApiResponse<Unit>> =
        service.deleteClothesRental(clothesId)
    suspend fun postClothesRental(clothesId: Int): Response<ApiResponse<Unit>> = service.postClothesRental(clothesId)
}
