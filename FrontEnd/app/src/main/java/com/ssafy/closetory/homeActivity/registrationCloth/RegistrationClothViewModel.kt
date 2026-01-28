package com.ssafy.closetory.homeActivity.registrationCloth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.RegistrationClothDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

private const val TAG = "RegistrationClothViewModel_싸피"

class RegistrationClothViewModel : ViewModel() {

    private val repository = RegistrationClothRepository()

    // 토스트 전용
    private val _message = MutableSharedFlow<String>(replay = 0)
    val message: SharedFlow<String?> = _message

    // 배경 제거된 이미지
    private val _maskedImageUrl = MutableLiveData<String?>()
    val maskedImageUrl: LiveData<String?> = _maskedImageUrl

    // 등록/수정 성공의 경우 clothesId를 이용해 상세 페이지 이동
    private val _navigateToDetail = MutableSharedFlow<Int>(replay = 0)
    val navigateToDetail: SharedFlow<Int> = _navigateToDetail

    fun clearMaskedUrl() {
        _maskedImageUrl.value = null
    }

    fun removeImageBackground(clothesPhoto: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "send binary part=$clothesPhoto")
                val res = repository.removeImageBackground(clothesPhoto)

                if (res.isSuccessful) {
                    val url = res.body()?.data?.maskedImageUrl
                    if (url.isNullOrBlank()) {
                        _message.emit("마스킹 응답이 비었습니다.")
                        return@launch
                    }
                    _maskedImageUrl.value = url
                } else {
                    _message.emit(res.body()?.errorMessage ?: "마스킹 실패")
                }
            } catch (e: Exception) {
                Log.d(TAG, "removeImageBackground Error: ${e.message}", e)
                _message.emit("네트워크 오류 발생")
            }
        }
    }

    // 옷 등록
    fun registrationCloth(photoUrl: String, tags: List<Int>, clothesTypes: Int, seasons: List<Int>, color: String) {
        viewModelScope.launch {
            try {
                val res = repository.registrationCloth(
                    RegistrationClothDto(
                        photoUrl = photoUrl,
                        tags = tags,
                        clothesTypes = clothesTypes,
                        seasons = seasons,
                        color = color
                    )
                )

                if (res.isSuccessful) {
                    _message.emit(res.body()?.responseMessage ?: "등록 성공")

                    // 등록 후 상세 페이지 이동 관련 로직
                    val newId = res.body()?.data?.clothesId
                    if (newId == null) return@launch
                    _navigateToDetail.emit(newId)
                } else {
                    _message.emit(res.body()?.errorMessage ?: "등록 실패")
                }
            } catch (e: Exception) {
                _message.emit(e.message ?: "네트워크 오류")
            }
        }
    }

    // 옷 수정
    fun patchCloth(
        clothesId: Int,
        photoUrl: String,
        tags: List<Int>,
        clothesTypes: Int,
        seasons: List<Int>,
        color: String
    ) {
        viewModelScope.launch {
            try {
                val res = repository.patchCloth(
                    clothesId,
                    RegistrationClothDto(
                        photoUrl = photoUrl,
                        tags = tags,
                        clothesTypes = clothesTypes,
                        seasons = seasons,
                        color = color
                    )
                )

                if (res.isSuccessful) {
                    _message.emit(res.body()?.responseMessage ?: "수정 성공")
                    _navigateToDetail.emit(clothesId)
                } else {
                    _message.emit(res.body()?.errorMessage ?: "수정 실패")
                }
            } catch (e: Exception) {
                _message.emit(e.message ?: "네트워크 오류")
            }
        }
    }
}
