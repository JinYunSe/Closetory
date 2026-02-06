package com.ssafy.closetory.homeActivity.aiStyling

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.AiCoordinationResponse
import com.ssafy.closetory.dto.AiFittingRequest
import com.ssafy.closetory.dto.SaveLookRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "AiStylingViewModel"

enum class AiStylingStage {
    RECOMMEND, // "AI 코디생성" 단계
    FITTING_READY, // 추천 완료 → "AI 가상생성" 가능
    FITTING_DONE // 가상피팅 완료 → 등록 가능
}

class AiStylingViewModel : ViewModel() {

    private val repository = AiStylingRepository()

    // 현재 실행 중인 Job 추적
    private var recommendJob: Job? = null
    private var fittingJob: Job? = null
    private var saveJob: Job? = null

    private val _stage = MutableLiveData(AiStylingStage.RECOMMEND)
    val stage: LiveData<AiStylingStage> = _stage

    private val _aiCoordination = MutableLiveData<AiCoordinationResponse?>()
    val aiCoordination: LiveData<AiCoordinationResponse?> = _aiCoordination

    private val _aiReason = MutableLiveData<String?>()
    val aiReason: LiveData<String?> = _aiReason

    private val _aiPhotoUrl = MutableLiveData<String?>()
    val aiPhotoUrl: LiveData<String?> = _aiPhotoUrl

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    // 로딩 타입 추적 (어떤 작업이 로딩 중인지)
    private val _loadingType = MutableLiveData<LoadingType?>()
    val loadingType: LiveData<LoadingType?> = _loadingType

    enum class LoadingType {
        RECOMMEND, // AI 추천 중
        FITTING, // 가상피팅 중
        SAVING // 저장 중
    }

    // AI 추천 요청
    fun requestAiRecommendation(isPersonalized: Boolean, onlyMine: Boolean) {
        // 이미 실행 중이면 무시
        if (recommendJob?.isActive == true) {
            Log.d(TAG, "AI 추천이 이미 실행 중입니다")
            return
        }

        recommendJob = viewModelScope.launch {
            _isLoading.value = true
            _loadingType.value = LoadingType.RECOMMEND

            try {
                Log.d(TAG, "AI 추천 시작: personalized=$isPersonalized, onlyMine=$onlyMine")

                val response = repository.getAiRecommendation(isPersonalized, onlyMine)

                if (response.isSuccessful) {
                    val body = response.body()
                    val isOk = body != null &&
                        body.data != null &&
                        (body.httpStatusCode in 200..299)
                    if (isOk) {
                        _aiCoordination.value = body?.data
                        _aiReason.value = body?.data?.aiReason
                        _successMessage.value = body?.responseMessage ?: "AI 코디가 완성 됐습니다"
                        _stage.value = AiStylingStage.FITTING_READY

                        Log.d(TAG, " AI 추천 성공 / stage=FITTING_READY")
                    } else {
                        _errorMessage.value = body?.errorMessage ?: "추천 결과가 비어있습니다."
                        Log.e(TAG, " AI 추천 실패: Body 또는 데이터 null")
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "요청 값이 올바르지 않습니다."
                        401 -> "인증 실패 (토큰 만료 등)"
                        403 -> "AI코디에 대한 권한이 없습니다."
                        else -> "AI 추천에 실패했습니다. (${response.code()})"
                    }
                    _errorMessage.value = errorMsg
                    Log.e(TAG, " AI 추천 실패: $errorMsg")
                }
            } catch (e: Exception) {
                _errorMessage.value = "AI recommendation error: ${e.message}"
                Log.e(TAG, " AI 추천 예외", e)
            } finally {
                _isLoading.value = false
                _loadingType.value = null
                recommendJob = null
                Log.d(TAG, "AI 추천 종료")
            }
        }
    }

    //  AI 가상피팅 요청
    fun requestAiFitting() {
        val coordination = _aiCoordination.value
        if (coordination == null) {
            _errorMessage.value = "가상피팅할 코디가 없습니다."
            Log.e(TAG, "가상피팅 실패: coordination이 null")
            return
        }

        // 이미 실행 중이면 무시
        if (fittingJob?.isActive == true) {
            Log.w(TAG, "가상피팅이 이미 실행 중입니다 - 중복 호출 차단")
            return
        }

        fittingJob = viewModelScope.launch {
            _isLoading.value = true
            _loadingType.value = LoadingType.FITTING

            try {
                Log.d(TAG, "가상피팅 시작")

                val orderedIds = buildFittingIdList(coordination)
                if (orderedIds.any { it == -1 }) {
                    _errorMessage.value = "Missing clothesId in fitting list."
                    Log.e(TAG, "???? ??: missing clothesId in list $orderedIds")
                    return@launch
                }
                Log.d(TAG, "피팅 요청 ID 리스트: $orderedIds")

                val request = AiFittingRequest(clothesIdList = orderedIds)
                val response = repository.requestAiFitting(request)

                Log.d(TAG, "API 응답 코드: ${response.code()}")
                Log.d(TAG, "API 응답 성공 여부: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d(TAG, "Response Body: $body")
                    Log.d(TAG, "Body httpStatusCode: ${body?.httpStatusCode}")
                    Log.d(TAG, "Body data: ${body?.data}")
                    Log.d(TAG, "Body data.aiPhotoUrl: ${body?.data?.aiPhotoUrl}")

                    if (body != null && body.data != null && (body.httpStatusCode in 200..299)) {
                        val photoUrl = body.data.aiPhotoUrl

                        if (photoUrl.isNullOrBlank()) {
                            _errorMessage.value = "가상피팅 이미지 URL이 비어있습니다."
                            Log.e(TAG, "aiPhotoUrl이 null 또는 빈 문자열")
                        } else {
                            _aiPhotoUrl.value = photoUrl
                            _successMessage.value = body.responseMessage ?: "가상 피팅 성공"
                            _stage.value = AiStylingStage.FITTING_DONE
                            Log.d(TAG, "가상피팅 성공 / url=$photoUrl")
                        }
                    } else {
                        val errorMsg = body?.errorMessage ?: "가상피팅 결과가 비어있습니다."
                        _errorMessage.value = errorMsg
                        Log.e(TAG, "가상피팅 실패: httpStatusCode=${body?.httpStatusCode}, data=${body?.data}")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = when (response.code()) {
                        400 -> "유효하지 않는 사용자입니다."
                        401 -> "인증 실패 (토큰 만료 등)"
                        else -> "가상피팅에 실패했습니다. (${response.code()})"
                    }
                    _errorMessage.value = errorMsg
                    Log.e(TAG, "가상피팅 API 실패: code=${response.code()}, error=$errorBody")
                }
            } catch (e: Exception) {
                _errorMessage.value = "가상피팅 중 오류가 발생했습니다: ${e.message}"
                Log.e(TAG, "가상피팅 예외 발생", e)
            } finally {
                _isLoading.value = false
                _loadingType.value = null
                fittingJob = null
                Log.d(TAG, "가상피팅 작업 종료")
            }
        }
    }

    // 룩 저장
    fun saveCurrentLook() {
        val coordination = _aiCoordination.value
        if (coordination == null) {
            _errorMessage.value = "현재 코디가 없습니다."
            return
        }

	    val aiPhotoUrl = _aiPhotoUrl.value
	    if (aiPhotoUrl.isNullOrBlank()) {
            _errorMessage.value = "AI 이미지가 아직 생성되지 않았습니다. 먼저 가상 피팅을 진행해 주세요."
            return
        }

        // 이미 실행 중이면 무시
        if (saveJob?.isActive == true) {
            Log.d(TAG, "저장이 이미 실행 중입니다")
            return
        }

        saveJob = viewModelScope.launch {
            _isLoading.value = true
            _loadingType.value = LoadingType.SAVING

            try {
                Log.d(TAG, "룩 저장 시작")

                val clothesIds = coordination.clothesIdList.map { it.clothesId }
                val request = SaveLookRequest(
                    clothesIdList = clothesIds,
	                    aiPhotoUrl = aiPhotoUrl,
                    aiReason = _aiReason.value
                )

                val response = repository.saveLook(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    _successMessage.value = body?.responseMessage ?: "룩이 저장되었습니다!"

                    Log.d(TAG, "룩 저장 성공")

                    // 저장 성공 후 초기화
                    resetAll()
                } else {
                    _errorMessage.value = "룩 저장에 실패했습니다. (${response.code()})"
                    Log.e(TAG, " 룩 저장 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "룩 저장 예외", e)
            } finally {
                _isLoading.value = false
                _loadingType.value = null
                saveJob = null
                Log.d(TAG, "룩 저장 종료")
            }
        }
    }

    // 전체초기화
    fun resetAll() {
        // 실행 중인 Job 취소
        recommendJob?.cancel()
        fittingJob?.cancel()
        saveJob?.cancel()

        _aiCoordination.value = null
        _aiReason.value = null
        _aiPhotoUrl.value = null
        _stage.value = AiStylingStage.RECOMMEND
        _isLoading.value = false
        _loadingType.value = null

        Log.d(TAG, "전체 초기화 완료")
    }

    // 작업이 진행 중인지 확인
    fun isAnyJobRunning(): Boolean = recommendJob?.isActive == true ||
        fittingJob?.isActive == true ||
        saveJob?.isActive == true

    private fun buildFittingIdList(coordination: AiCoordinationResponse): List<Int> {
        val map = coordination.clothesIdList.associateBy { type ->
            when (type.clothesType.uppercase()) {
                "ACC" -> "ACCESSORIES"
                else -> type.clothesType.uppercase()
            }
        }
        val order = listOf("TOP", "BOTTOM", "SHOES", "OUTER", "ACCESSORIES", "BAG")
        return order.map { type -> map[type]?.clothesId ?: -1 }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // ViewModel이 파괴될 때 모든 Job 취소
        recommendJob?.cancel()
        fittingJob?.cancel()
        saveJob?.cancel()
        Log.d(TAG, "ViewModel cleared - 모든 Job 취소")
    }
}
