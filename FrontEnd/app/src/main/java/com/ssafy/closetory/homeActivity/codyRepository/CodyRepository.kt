package com.ssafy.closetory.homeActivity.codyRepository

import android.util.Log
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.CodyRepositoryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

private const val TAG = "CodyRepository"

class CodyRepository(private val service: CodyRepositoryService) {

    suspend fun getLooks(): Result<List<CodyRepositoryResponse>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "API 호출 시작 - GET /looks")

            val response: Response<ApiResponse<List<CodyRepositoryResponse>>> = service.getLooks()

            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "응답 성공 - httpStatusCode: ${body?.httpStatusCode}")

                if (body != null && body.data != null) {
                    Log.d(TAG, "데이터 수신 성공 - ${body.data.size}개 아이템")
                    Result.success(body.data)
                } else {
                    Log.e(TAG, "응답 body 또는 data가 null")
                    Result.failure(Exception("데이터가 없습니다"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "API 응답 실패 - 코드: ${response.code()}, 메시지: $errorBody")
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "네트워크 예외 발생", e)
            Result.failure(e)
        }
    }
}
