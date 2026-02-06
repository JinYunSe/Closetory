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
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "StylingViewModel"

enum class StylingStage {
    SELECTING, // 옷 선택 중
    FITTING_READY, // 옷 선택 완료 → "AI 가상피팅" 가능
    FITTING_DONE, // 가상피팅 완료 → "등록" 가능
    SAVED // 저장 완료 → "코디 저장소 가기" 가능
}

class StylingViewModel : ViewModel() {

    private val repository = StylingRepository()

    // 현재 실행 중인 Job 추적
    private var fittingJob: Job? = null
    private var saveJob: Job? = null
    private var lastSavedAiPhotoUrl: String? = null

    // 슬롯 데이터를 ViewModel에 저장 (Fragment 재생성 시에도 유지)
    val selectedSlots = mutableMapOf<String, ClothesItemDto?>(
        "TOP" to null,
        "BOTTOM" to null,
        "SHOES" to null,
        "OUTER" to null,
        "ACCESSORIES" to null,
        "BAG" to null
    )

    // 단계 관리
    private val _stage = MutableLiveData(StylingStage.SELECTING)
    val stage: LiveData<StylingStage> = _stage

    private val _aiPhotoUrl = MutableLiveData<String?>()
    val aiPhotoUrl: LiveData<String?> = _aiPhotoUrl

    private val _closetData = MutableLiveData<ClosetResponse?>()
    val closetData: LiveData<ClosetResponse?> = _closetData

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    // 저장 완료 후 코디저장소 이동 트리거
    private val _navigateToLookStorage = MutableLiveData<Boolean>(false)
    val navigateToLookStorage: LiveData<Boolean> = _navigateToLookStorage

    // 로딩 타입 추적
    private val _loadingType = MutableLiveData<LoadingType?>()
    val loadingType: LiveData<LoadingType?> = _loadingType

    enum class LoadingType {
        FITTING, // 가상피팅 중
        SAVING // 저장 중
    }

    /**
     * ⭐ 의류 리스트 조회
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

                    Log.d(TAG, "✅ loadClothItems 성공: $data")
                    _closetData.value = data
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "❌ loadClothItems 실패 - 코드: ${response.code()}, 메시지: $errorBody")
                    _errorMessage.value = "의류 정보를 불러오는데 실패했습니다"
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ loadClothItems 예외 발생", e)
            }
        }
    }

    /**
     * ⭐ AI 가상피팅 요청
     */
    fun requestAiFitting(clothesIdList: List<Int>) {
        if (clothesIdList.all { it == -1 }) {
            _errorMessage.value = "최소 1개 이상의 의류를 선택해 주세요."
            return
        }

        // 이미 실행 중이면 무시
        if (fittingJob?.isActive == true) {
            Log.d(TAG, "⚠️ 가상피팅이 이미 실행 중입니다")
            return
        }

        fittingJob = viewModelScope.launch {
            _isLoading.value = true
            _loadingType.value = LoadingType.FITTING
            _aiPhotoUrl.value = null

            try {
                Log.d(TAG, "🎬 가상피팅 시작 (타임아웃 3분)")
                Log.d(TAG, "📤 요청 데이터: $clothesIdList")

                val request = AiFittingRequest(clothesIdList)
                val response = repository.requestAiFitting(request)

                Log.d(TAG, "📥 응답 수신: code=${response.code()}, isSuccessful=${response.isSuccessful}")

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d(TAG, "📦 응답 body: $body")

                    if (body != null && body.data != null && (body.httpStatusCode in 200..299)) {
                        val photoUrl = body.data.aiPhotoUrl

                        if (!photoUrl.isNullOrBlank()) {
                            _aiPhotoUrl.value = photoUrl
                            _successMessage.value = body.responseMessage ?: "가상 피팅 성공!"
                            _stage.value = StylingStage.FITTING_DONE

                            Log.d(TAG, "가상피팅 성공!")
                            Log.d(TAG, "이미지 URL: $photoUrl")
                        } else {
                            _errorMessage.value = "AI 이미지 URL이 비어있습니다."
                            Log.e(TAG, "❌ aiPhotoUrl이 null 또는 빈 값")
                        }
                    } else {
                        _errorMessage.value = body?.errorMessage ?: "가상피팅 결과가 비어있습니다."
                        Log.e(TAG, "❌ 응답 데이터 이상: httpStatusCode=${body?.httpStatusCode}, data=${body?.data}")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = when (response.code()) {
                        400 -> "잘못된 요청입니다. 옷 선택을 확인해 주세요."
                        401 -> "인증이 만료되었습니다. 다시 로그인해 주세요."
                        408 -> "요청 시간이 초과되었습니다. 다시 시도해 주세요."
                        500 -> "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."
                        503 -> "서버가 일시적으로 사용 불가능합니다."
                        else -> "가상피팅에 실패했습니다. (${response.code()})"
                    }
                    _errorMessage.value = errorMsg
                    Log.e(TAG, "❌ HTTP 에러: code=${response.code()}, body=$errorBody")
                }
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "❌ 타임아웃 에러", e)
            } catch (e: UnknownHostException) {
                Log.e(TAG, "❌ 네트워크 연결 에러", e)
            } catch (e: Exception) {
                Log.e(TAG, "❌ 가상피팅 예외", e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                _loadingType.value = null
                fittingJob = null
                Log.d(TAG, "🎬 가상피팅 종료")
            }
        }
    }

    /**
     * ⭐ 룩 저장 (개선된 UX - 저장 후 초기화하고 코디저장소 이동 가능)
     */
    fun saveLook(clothesIdList: List<Int>, autoSave: Boolean = false) {
        if (clothesIdList.all { it == -1 }) {
            _errorMessage.value = "최소 1개 이상의 의류를 선택해 주세요."
            return
        }

        val aiPhotoUrl = _aiPhotoUrl.value
        if (aiPhotoUrl.isNullOrBlank()) {
            _errorMessage.value = "AI 이미지가 없습니다. 먼저 가상 피팅을 진행해 주세요."
            return
        }

        if (aiPhotoUrl == lastSavedAiPhotoUrl) {
            Log.d(TAG, "이미 저장된 코디입니다. 중복 저장 방지")
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
                    aiPhotoUrl = aiPhotoUrl,
                    aiReason = null // 직접 코디는 AI 이유 없음
                )

                Log.d(TAG, "saveLook 요청: $request")

                val response = repository.saveLook(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d(TAG, " 룩 저장 성공: ${body?.data}")
                    if (!autoSave) {
                        _successMessage.value = body?.responseMessage ?: "코디가 저장되었습니다!"
                    }
                    lastSavedAiPhotoUrl = aiPhotoUrl

                    if (!autoSave) {
                        // ✅ 핵심: 전체 초기화(resetAll) 금지
                        // 등록 완료 후에는 "가상피팅 결과만" 지우고,
                        // 다시 가상피팅 가능한 상태(FITTING_READY)로 돌려준다.
                        _aiPhotoUrl.value = null
                        _stage.value = StylingStage.SAVED
                        Log.d(TAG, " 등록 완료 → SAVED로 전환 (코디 저장소 이동 가능)")
                    } else {
                        Log.d(TAG, " 자동 저장 완료 (UI 유지)")
                    }

                    // (선택) 남아있는 로딩/에러 상태 정리
                    _errorMessage.value = null
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, " 룩 저장 실패 - 코드: ${response.code()}, 메시지: $errorBody")
                    _errorMessage.value = "코디 저장에 실패했습니다"
                }
            } catch (e: Exception) {
                Log.e(TAG, " 룩 저장 예외", e)
            } finally {
                _isLoading.value = false
                _loadingType.value = null
                saveJob = null
                Log.d(TAG, " 룩 저장 종료")
            }
        }
    }

    /**
     * 🆕 저장 완료 후 다시 코디 만들기 (초기 상태로 복귀)
     */
    fun resetToCreateNewLook() {
        // 슬롯 데이터 초기화
        selectedSlots.clear()
        selectedSlots["TOP"] = null
        selectedSlots["BOTTOM"] = null
        selectedSlots["SHOES"] = null
        selectedSlots["OUTER"] = null
        selectedSlots["ACCESSORIES"] = null
        selectedSlots["BAG"] = null

        _aiPhotoUrl.value = null
        _stage.value = StylingStage.SELECTING
        _isLoading.value = false
        _loadingType.value = null

        Log.d(TAG, "🔄 저장 완료 후 초기화 - 새 코디 만들기 준비")
    }

    /**
     * 🆕 코디저장소로 이동 트리거
     */
    fun navigateToLookStorage() {
        _navigateToLookStorage.value = true
        Log.d(TAG, "🏪 코디저장소 이동 트리거")
    }

    /**
     * 🆕 코디저장소 이동 완료 처리
     */
    fun onNavigatedToLookStorage() {
        _navigateToLookStorage.value = false
        // 이동 후 초기화
        resetToCreateNewLook()
    }

    /**
     * 완료되어 사진이 나온 뒤 다른 탭 갔다 오면 초기화
     */
    fun resetAfterFittingDoneIfNeeded() {
        val isDone = _stage.value == StylingStage.FITTING_DONE
        val hasResult = !_aiPhotoUrl.value.isNullOrBlank()

        if (!isDone || !hasResult) return

        _stage.value = StylingStage.FITTING_READY
        _aiPhotoUrl.value = null
        _loadingType.value = null
        _isLoading.value = false
        _errorMessage.value = null

        Log.d(TAG, "🔄 가상피팅 완료 상태 → 화면 이탈로 초기화")
    }

    /**
     * ⭐ 전체 초기화 (초기화 버튼용)
     */
    fun resetAll() {
        // 실행 중인 Job 취소
        fittingJob?.cancel()
        saveJob?.cancel()

        // 슬롯 데이터 초기화
        selectedSlots.clear()
        selectedSlots["TOP"] = null
        selectedSlots["BOTTOM"] = null
        selectedSlots["SHOES"] = null
        selectedSlots["OUTER"] = null
        selectedSlots["ACCESSORIES"] = null
        selectedSlots["BAG"] = null

        _aiPhotoUrl.value = null
        _stage.value = StylingStage.SELECTING
        _isLoading.value = false
        _loadingType.value = null

        Log.d(TAG, "🔄 전체 초기화 완료")
    }

    /**
     * 옷 선택 시 단계 업데이트
     */
    fun updateStageAfterSelection(hasSelection: Boolean) {
        // SAVED 상태일 때는 선택하면 바로 FITTING_READY로 전환
        val currentStage = _stage.value

        if (hasSelection) {
            if (currentStage == StylingStage.SELECTING || currentStage == StylingStage.SAVED) {
                _stage.value = StylingStage.FITTING_READY
                Log.d(TAG, "📍 단계 변경: FITTING_READY")
            }
        } else {
            _stage.value = StylingStage.SELECTING
            Log.d(TAG, "📍 단계 변경: SELECTING")
        }
    }

    fun syncStageWithCurrentState() {
        val hasPhoto = !_aiPhotoUrl.value.isNullOrBlank()
        val hasSelection = selectedSlots.values.any { it != null }

        _stage.value = when {
            hasPhoto -> StylingStage.FITTING_DONE
            hasSelection -> StylingStage.FITTING_READY
            else -> StylingStage.SELECTING
        }

        Log.d(TAG, "📍 단계 동기화: photo=$hasPhoto, selection=$hasSelection, stage=${_stage.value}")
    }

    /**
     * 작업이 진행 중인지 확인
     */
    fun isAnyJobRunning(): Boolean {
        val isRunning = fittingJob?.isActive == true || saveJob?.isActive == true
        if (isRunning) {
            Log.d(TAG, "⚡ Job 실행 중 - fitting: ${fittingJob?.isActive}, save: ${saveJob?.isActive}")
        }
        return isRunning
    }

    fun clearAiFittingResult() {
        _aiPhotoUrl.value = null
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
