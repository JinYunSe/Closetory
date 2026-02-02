package com.ssafy.closetory.homeActivity.styling

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.AiFittingRequest
import com.ssafy.closetory.dto.ClosetResponse
import com.ssafy.closetory.dto.ClothesItemDto
import com.ssafy.closetory.dto.SaveLookRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "StylingViewModel"

enum class StylingStage {
    SELECTING, // 옷 선택 중
    FITTING_READY, // 옷 선택 완료 → "AI 가상피팅" 가능
    FITTING_DONE // 가상피팅 완료 → "등록" 가능
}

class StylingViewModel : ViewModel() {

    private val repository = StylingRepository()

    // 현재 실행 중인 Job 추적
    private var fittingJob: Job? = null
    private var saveJob: Job? = null

    // 슬롯 데이터를 ViewModel에 저장 (Fragment 재생성 시에도 유지)
    val selectedSlots = mutableMapOf<String, ClothesItemDto?>(
        "TOP" to null,
        "BOTTOM" to null,
        "SHOES" to null,
        "OUTER" to null,
        "ACC" to null,
        "BAG" to null
    )

    // 단계 관리
    private val _stage = MutableLiveData(StylingStage.SELECTING)
    val stage: LiveData<StylingStage> = _stage

    private val _aiImageUrl = MutableLiveData<String?>()
    val aiImageUrl: LiveData<String?> = _aiImageUrl

    private val _closetData = MutableLiveData<ClosetResponse?>()
    val closetData: LiveData<ClosetResponse?> = _closetData

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    // 로딩 타입 추적
    private val _loadingType = MutableLiveData<LoadingType?>()
    val loadingType: LiveData<LoadingType?> = _loadingType

    enum class LoadingType {
        FITTING, // 가상피팅 중
        SAVING // 저장 중
    }

    /**
     * 의류 리스트 조회
     */
    fun loadClothItems(onlyMine: Boolean = false) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null

                val response = repository.getClothesList(
                    tags = null,
                    color = null,
                    seasons = null,
                    onlyMine = onlyMine
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    val data = body?.data

                    Log.d(TAG, "loadClothItems 성공: $data")
                    _closetData.value = data
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "loadClothItems 실패 - 코드: ${response.code()}, 메시지: $errorBody")
                    _errorMessage.value = "의류 정보를 불러오는데 실패했습니다"
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadClothItems 예외 발생", e)
                _errorMessage.value = "네트워크 오류: ${e.message}"
            }
        }
    }

    // AI 가상피팅 요청 (비동기 처리)

    fun requestAiFitting(clothesIdList: List<Int>) {
        if (clothesIdList.all { it == -1 }) {
            _errorMessage.value = "최소 1개 이상의 의류를 선택해주세요."
            return
        }

        // 이미 실행 중이면 무시
        if (fittingJob?.isActive == true) {
            Log.d(TAG, "⚠가상피팅이 이미 실행 중입니다")
            return
        }

        fittingJob = viewModelScope.launch {
            _isLoading.value = true
            _loadingType.value = LoadingType.FITTING
            _aiImageUrl.value = null

            try {
                Log.d(TAG, "🎬 가상피팅 시작 (비동기)")

                val request = AiFittingRequest(clothesIdList)
                val response = repository.requestAiFitting(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.httpStatusCode == 201 && body.data != null) {
                        _aiImageUrl.value = body.data.aiImageUrl
                        _successMessage.value = body.responseMessage ?: "가상 피팅 성공!"
                        _stage.value = StylingStage.FITTING_DONE

                        Log.d(TAG, "가상피팅 성공 / url=${body.data.aiImageUrl}")
                    } else {
                        _errorMessage.value = body?.errorMessage ?: "가상피팅 결과가 비어있습니다."
                        Log.e(TAG, "가상피팅 실패: Body 또는 데이터 null")
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "유효하지 않는 사용자입니다."
                        401 -> "인증 실패 (토큰 만료 등)"
                        else -> "가상피팅에 실패했습니다. (${response.code()})"
                    }
                    _errorMessage.value = errorMsg
                    Log.e(TAG, "가상피팅 실패: $errorMsg")
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류: ${e.message}"
                Log.e(TAG, "가상피팅 예외", e)
            } finally {
                _isLoading.value = false
                _loadingType.value = null
                fittingJob = null
                Log.d(TAG, "가상피팅 종료")
            }
        }
    }

    // 룩 저장

    fun saveLook(clothesIdList: List<Int>) {
        if (clothesIdList.all { it == -1 }) {
            _errorMessage.value = "최소 1개 이상의 의류를 선택해주세요"
            return
        }

        val aiImageUrl = _aiImageUrl.value
        if (aiImageUrl.isNullOrBlank()) {
            _errorMessage.value = "AI 이미지가 없습니다. 먼저 가상 피팅을 진행해 주세요."
            return
        }

        // 이미 실행 중이면 무시
        if (saveJob?.isActive == true) {
            Log.d(TAG, " 저장이 이미 실행 중입니다")
            return
        }

        saveJob = viewModelScope.launch {
            _isLoading.value = true
            _loadingType.value = LoadingType.SAVING

            try {
                Log.d(TAG, " 룩 저장 시작")

                val request = SaveLookRequest(
                    clothesIdList = clothesIdList.filter { it != -1 },
                    aiImageUrl = aiImageUrl,
                    aiReason = null // 직접 코디는 AI 이유 없음
                )

                Log.d(TAG, "saveLook 요청: $request")

                val response = repository.saveLook(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d(TAG, " 룩 저장 성공: ${body?.data}")
                    _successMessage.value = body?.responseMessage ?: "코디가 저장되었습니다!"

                    // 저장 성공 후 초기화
                    resetAll()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, " 룩 저장 실패 - 코드: ${response.code()}, 메시지: $errorBody")
                    _errorMessage.value = "코디 저장에 실패했습니다"
                }
            } catch (e: Exception) {
                Log.e(TAG, " 룩 저장 예외", e)
                _errorMessage.value = "네트워크 오류: ${e.message}"
            } finally {
                _isLoading.value = false
                _loadingType.value = null
                saveJob = null
                Log.d(TAG, " 룩 저장 종료")
            }
        }
    }

    // 완료되어 사진이 나온 뒤에 다른 탭 갔다 오면 초기화
    fun resetAfterFittingDoneIfNeeded() {
        // '완료 + 결과 있음' 상태일 때만 초기화
        val isDone = _stage.value == StylingStage.FITTING_DONE
        val hasResult = !_aiImageUrl.value.isNullOrBlank()

        if (!isDone || !hasResult) return

        // 완료 상태를 초기 상태로 되돌림
        _stage.value = StylingStage.FITTING_READY // 또는 RECOMMEND/초기 단계 (프로젝트 흐름에 맞춰)
        _aiImageUrl.value = null
        _loadingType.value = null
        _isLoading.value = false
        _errorMessage.value = null

        Log.d(TAG, " 가상피팅 완료 상태 → 화면 이탈로 초기화 처리")
    }

    // 전체 초기화
    fun resetAll() {
        // 실행 중인 Job 취소
        fittingJob?.cancel()
        saveJob?.cancel()

        // 슬롯 데이터도 초기화
        selectedSlots.clear()
        selectedSlots["TOP"] = null
        selectedSlots["BOTTOM"] = null
        selectedSlots["SHOES"] = null
        selectedSlots["OUTER"] = null
        selectedSlots["ACC"] = null
        selectedSlots["BAG"] = null

        _aiImageUrl.value = null
        _stage.value = StylingStage.SELECTING
        _isLoading.value = false
        _loadingType.value = null

        Log.d(TAG, " 전체 초기화 완료")
    }

    // 옷 선택 시 단계 업데이트

    fun updateStageAfterSelection(hasSelection: Boolean) {
        if (hasSelection && _stage.value == StylingStage.SELECTING) {
            _stage.value = StylingStage.FITTING_READY
            Log.d(TAG, "📍 단계 변경: FITTING_READY")
        } else if (!hasSelection) {
            _stage.value = StylingStage.SELECTING
            Log.d(TAG, "📍 단계 변경: SELECTING")
        }
    }

    // 작업이 진행 중인지 확인 (비동기 처리 여부)
    fun isAnyJobRunning(): Boolean {
        val isRunning = fittingJob?.isActive == true || saveJob?.isActive == true
        if (isRunning) {
            Log.d(TAG, "⚡ Job 실행 중 - fittingJob: ${fittingJob?.isActive}, saveJob: ${saveJob?.isActive}")
        }
        return isRunning
    }

    fun clearAiFittingResult() {
        _aiImageUrl.value = null
        _stage.value = StylingStage.FITTING_READY
        Log.d(TAG, "🗑️ AI 가상 피팅 결과 초기화")
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        fittingJob?.cancel()
        saveJob?.cancel()
        Log.d(TAG, "🧹 ViewModel cleared - 모든 Job 취소")
    }
}
