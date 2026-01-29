package com.ssafy.closetory.homeActivity.closet

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.ClosetResponse
import com.ssafy.closetory.dto.ClothesItemDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ClosetService {

    @GET("clothes")
    suspend fun getClothesList(
        @Query("tags") tags: List<Int>?,
        @Query("color") color: String?,
        @Query("seasons") seasons: List<Int>?,
        @Query("onlyMine") onlyMine: Boolean?
    ): Response<ApiResponse<ClosetResponse>>

    @GET("clothes/{clothesId}")
    suspend fun getClothesDetail(@Path("clothesId") clothesId: Int): Response<ApiResponse<ClothesItemDto>>

    @DELETE("clothes/{clothesId}")
    suspend fun deleteClothes(@Path("clothesId") clothesId: Int): Response<ApiResponse<Unit>>
}
