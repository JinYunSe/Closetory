package com.ssafy.closetory.homeActivity.codyRepository

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.CodyRepositoryResponse
import com.ssafy.closetory.dto.UpdateLookRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface CodyRepositoryService {

    // 룩 목록 조회

    @GET("looks")
    suspend fun getLooks(): Response<ApiResponse<List<CodyRepositoryResponse>>>

    // 룩 수정 (캘린더 날짜 등록)
    // PATCH /api/v1/looks/{lookId}
    @PATCH("looks/{lookId}")
    suspend fun updateLook(@Path("lookId") lookId: Int, @Body request: UpdateLookRequest): Response<ApiResponse<Unit>>

    // 룩 삭제
    // DELETE /api/v1/looks/{lookId}
    @DELETE("looks/{lookId}")
    suspend fun deleteLook(@Path("lookId") lookId: Int): Response<ApiResponse<Unit>>
}
