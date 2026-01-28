package com.ssafy.closetory.homeActivity.aiStyling

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.AiCoordinationResponse
import com.ssafy.closetory.dto.AiFittingRequest
import com.ssafy.closetory.dto.SaveLookRequest
import kotlinx.coroutines.launch

private const val TAG = "AiStylingViewModel_싸피"

enum class AiStylingStage {
    RECOMMEND, // "AI 코디생성" 단계
    FITTING_READY, // 추천 슬롯 채워짐 → "AI 가상생성" 가능
    FITTING_DONE // 가상피팅 이미지 생성됨 → 등록 가능
}

class AiStylingViewModel : ViewModel() {

    private val repository = AiStylingRepository()

    private val _stage = MutableLiveData(AiStylingStage.RECOMMEND)
    val stage: LiveData<AiStylingStage> = _stage

    private val _aiCoordination = MutableLiveData<AiCoordinationResponse?>()
    val aiCoordination: LiveData<AiCoordinationResponse?> = _aiCoordination

    private val _aiReason = MutableLiveData<String?>()
    val aiReason: LiveData<String?> = _aiReason

    private val _aiImageUrl = MutableLiveData<String?>()
    val aiImageUrl: LiveData<String?> = _aiImageUrl

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    fun requestAiRecommendation(isPersonalized: Boolean, onlyMine: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getAiRecommendation(isPersonalized, onlyMine)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.httpStatusCode == 201 && body.data != null) {
                        _aiCoordination.value = body.data
                        _aiReason.value = body.data.aiReason
                        _successMessage.value = body.responseMessage ?: "AI 코디가 완성 됐습니다"

                        // 추천 성공 → 가상피팅 준비 상태
                        _stage.value = AiStylingStage.FITTING_READY
                        Log.d(TAG, "AI 추천 성공 / stage=FITTING_READY")
                    } else {
                        _errorMessage.value = body?.errorMessage ?: "추천 결과가 비어있습니다."
                    }
                } else {
                    _errorMessage.value = when (response.code()) {
                        400 -> "요청 값이 올바르지 않습니다."
                        401 -> "인증 실패 (토큰 만료 등)"
                        403 -> "AI코디에 대한 권한이 없습니다."
                        else -> "AI 추천에 실패했습니다. (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류가 발생했습니다: ${e.message}"
                Log.e(TAG, "AI 추천 예외", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun requestAiFitting() {
        val coordination = _aiCoordination.value
        if (coordination == null) {
            _errorMessage.value = "가상피팅할 코디가 없습니다."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val orderedIds = buildFittingIdList(coordination)
                val request = AiFittingRequest(clothesIdList = orderedIds)

                val response = repository.requestAiFitting(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.httpStatusCode == 201 && body.data != null) {
                        _aiImageUrl.value = body.data.aiImageUrl
                        _successMessage.value = body.responseMessage ?: "가상 피팅 성공"
                        _stage.value = AiStylingStage.FITTING_DONE
                        Log.d(TAG, "가상피팅 성공 / stage=FITTING_DONE / url=${body.data.aiImageUrl}")
                    } else {
                        _errorMessage.value = body?.errorMessage ?: "가상피팅 결과가 비어있습니다."
                    }
                } else {
                    _errorMessage.value = when (response.code()) {
                        400 -> "유효하지 않는 사용자입니다."
                        401 -> "인증 실패 (토큰 만료 등)"
                        else -> "가상피팅에 실패했습니다. (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류: ${e.message}"
                Log.e(TAG, "가상피팅 예외", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Top, Bottom, Shoes, Outer, Accessory, Bag 순서로 clothIdList 만들기
    // 없으면 -1
    private fun buildFittingIdList(coordination: AiCoordinationResponse): List<Int> {
        val map = coordination.clothIdList.associateBy { it.clothesType.uppercase() }
        val order = listOf("TOP", "BOTTOM", "SHOES", "OUTER", "ACCESSORY", "BAG")
        return order.map { type -> map[type]?.clothesId ?: -1 }
    }

    fun saveCurrentLook() {
        val coordination = _aiCoordination.value
        if (coordination == null) {
            _errorMessage.value = "현재 코디가 없습니다."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val clothIds = coordination.clothIdList.map { it.clothesId }
                val request = SaveLookRequest(clothesIdList = clothIds)

                val response = repository.saveLook(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    _successMessage.value = body?.responseMessage ?: "룩이 저장되었습니다!"
                    resetAll()
                } else {
                    _errorMessage.value = "룩 저장에 실패했습니다. (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류: ${e.message}"
                Log.e(TAG, "룩 저장 예외", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetAll() {
        _aiCoordination.value = null
        _aiReason.value = null
        _aiImageUrl.value = null
        _stage.value = AiStylingStage.RECOMMEND
        Log.d(TAG, "전체 초기화 / stage=RECOMMEND")
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
