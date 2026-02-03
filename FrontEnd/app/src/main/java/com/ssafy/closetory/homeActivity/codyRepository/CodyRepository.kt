package com.ssafy.closetory.homeActivity.codyRepository

import android.util.Log
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.CodyRepositoryResponse
import com.ssafy.closetory.dto.UpdateLookRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

private const val TAG = "CodyRepository"

class CodyRepository(private val service: CodyRepositoryService) {

    /**
     * 룩 목록 조회
     */
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

    /**
     * 캘린더에 룩 등록 (날짜 수정)
     * PATCH /looks/{lookId}
     */
    suspend fun registerToCalendar(lookId: Int, date: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🗓️ 캘린더 등록 시작 - lookId: $lookId, date: $date")

            val request = UpdateLookRequest(date = date)
            val response = service.updateLook(lookId, request)

            if (response.isSuccessful) {
                Log.d(TAG, "✅ 캘린더 등록 성공")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = when (response.code()) {
                    403 -> "자신의 옷으로 구성된 룩만 등록할 수 있습니다"
                    404 -> "존재하지 않는 룩입니다"
                    else -> "캘린더 등록 실패: ${response.code()}"
                }
                Log.e(TAG, "❌ $errorMsg - $errorBody")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 캘린더 등록 예외", e)
            Result.failure(e)
        }
    }

    /**
     * 룩 삭제
     * DELETE /looks/{lookId}
     */
    suspend fun deleteLook(lookId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🗑️ 룩 삭제 시작 - lookId: $lookId")

            val response = service.deleteLook(lookId)

            if (response.isSuccessful) {
                Log.d(TAG, "✅ 룩 삭제 성공")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = when (response.code()) {
                    400 -> "존재하지 않는 룩입니다"
                    403 -> "자신의 룩만 삭제할 수 있습니다"
                    else -> "룩 삭제 실패: ${response.code()}"
                }
                Log.e(TAG, "❌ $errorMsg - $errorBody")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 룩 삭제 예외", e)
            Result.failure(e)
        }
    }
}
