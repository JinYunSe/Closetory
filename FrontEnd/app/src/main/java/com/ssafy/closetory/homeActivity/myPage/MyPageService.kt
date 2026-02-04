package com.ssafy.closetory.homeActivity.mypage

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.CodyRepositoryResponse
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.dto.EditProfilePasswordCheckRequest
import com.ssafy.closetory.dto.StatisticsResponse
import com.ssafy.closetory.dto.Top3ClothesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MyPageService {

    // 현재 유저정보 불러오기
    @GET("users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: Int): Response<ApiResponse<EditProfileInfoResponse>>

    // 비밀번호 검증
    @POST("users/{userId}/password")
    suspend fun checkPassword(
        @Path("userId") userId: Int,
        @Body request: EditProfilePasswordCheckRequest
    ): Response<ApiResponse<Unit>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @GET("users/{userId}/stats/tag")
    suspend fun getTagsStatistics(@Path("userId") userId: Int): Response<ApiResponse<List<StatisticsResponse>>>

    @GET("users/{userId}/stats/color")
    suspend fun getColorsStatistics(@Path("userId") userId: Int): Response<ApiResponse<List<StatisticsResponse>>>

    @GET("users/{userId}/stats/top3")
    suspend fun getTop3Clothes(@Path("userId") userId: Int): Response<ApiResponse<List<Top3ClothesResponse>>>

    // 최근 코디 3개 조회
    @GET("looks")
    suspend fun getRecentCody(
        @Query("limit") limit: Int = 3,
        @Query("sort") sort: String = "date,desc"
    ): Response<ApiResponse<List<CodyRepositoryResponse>>>
}
