package com.ssafy.closetory.homeActivity.styling

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.AiFittingRequest
import com.ssafy.closetory.dto.ClosetResponse
import com.ssafy.closetory.dto.SaveLookRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "StylingViewModel_싸피"

class StylingViewModel : ViewModel() {

    private val _aiImageUrl = MutableLiveData<String?>()
    val aiImageUrl: LiveData<String?> = _aiImageUrl

    private val repository = StylingRepository()

    // 의류 데이터
    private val _closetData = MutableLiveData<ClosetResponse?>()
    val closetData: LiveData<ClosetResponse?> = _closetData

    // 로딩 상태
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 에러 메시지
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // 성공 메시지
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    /**
     * 의류 리스트 조회
     */
    fun loadClothItems(onlyMine: Boolean = false) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
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
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 룩 저장
     * 순서: Top, Bottom, Shoes, Outer, Accessory, Bag
     */
    fun saveLook(clothesIdList: List<Int>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // 빈 리스트 체크
                if (clothesIdList.all { it == -1 }) {
                    _errorMessage.value = "최소 1개 이상의 의류를 선택해주세요"
                    _isLoading.value = false
                    return@launch
                }

                val request = SaveLookRequest(clothesIdList = clothesIdList)

                Log.d(TAG, "saveLook 요청: $request")

                val response = repository.saveLook(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d(TAG, "saveLook 성공: ${body?.data}")
                    _successMessage.value = "코디가 저장되었습니다!"
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "saveLook 실패 - 코드: ${response.code()}, 메시지: $errorBody")
                    _errorMessage.value = "코디 저장에 실패했습니다"
                }
            } catch (e: Exception) {
                Log.e(TAG, "saveLook 예외 발생", e)
                _errorMessage.value = "네트워크 오류: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // AI 가상피팅 결과 URL

    fun requestAiFitting(clothesIdList: List<Int>) { // List<Long> → List<Int>

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _aiImageUrl.value = null

                if (clothesIdList.all { it == -1 }) { // .toInt() 제거

                    _errorMessage.value = "최소 1개 이상의 의류를 선택해주세요."
                    return@launch
                }

                val request = AiFittingRequest(clothesIdList)

                val response = repository.requestAiFitting(request)

                if (response.isSuccessful) {
                    val url = response.body()?.data?.aiImageUrl
                    _aiImageUrl.value = url
                    _successMessage.value = "가상 피팅 성공!"
                } else {
                    _errorMessage.value = "가상피팅에 실패했습니다."
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 가상생성페이지도 모두 내리는 코드
    fun clearAiFittingResult() {
        _aiImageUrl.value = null
        Log.d(TAG, "AI 가상 피팅 결과 초기화")
    }
}
