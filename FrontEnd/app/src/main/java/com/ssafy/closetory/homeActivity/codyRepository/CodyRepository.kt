package com.ssafy.closetory.homeActivity.codyRepository

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.CodyRepositoryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class CodyRepository(private val service: CodyRepositoryService) {

    suspend fun getLooks(): Result<List<CodyRepositoryResponse>> = withContext(Dispatchers.IO) {
        try {
            val response: Response<ApiResponse<List<CodyRepositoryResponse>>> = service.getLooks()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("데이터가 없습니다"))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
