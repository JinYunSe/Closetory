package com.ssafy.closetory.homeActivity.registrationClothes

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.PhotoUrlDto
import com.ssafy.closetory.dto.RegistrationClothesDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

private const val TAG = "RegistrationClothViewModel_싸피"

class RegistrationClothesViewModel : ViewModel() {

    private val repository = RegistrationClothesRepository()

    // 토스트 전용
    private val _message = MutableSharedFlow<String>(replay = 0)
    val message: SharedFlow<String?> = _message

    // 배경 제거된 이미지 혹은 개선된 이미지
    private val _imageUrl = MutableLiveData<String?>()
    val imageUrl: LiveData<String?> = _imageUrl

    // 등록/수정 성공의 경우 clothesId를 이용해 상세 페이지 이동
    private val _navigateToDetail = MutableSharedFlow<Int>(replay = 0)
    val navigateToDetail: SharedFlow<Int> = _navigateToDetail

    fun clearMaskedUrl() {
        _imageUrl.value = null
    }

    fun removeImageBackground(clothesPhoto: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "send binary part=$clothesPhoto")
                val res = repository.removeImageBackground(clothesPhoto)

                if (res.isSuccessful) {
                    val url = res.body()?.data?.photoUrl
                    if (url.isNullOrBlank()) {
                        _message.emit("마스킹 응답이 비었습니다.")
                        return@launch
                    }
                    _imageUrl.value = url
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
    fun registrationCloth(photoUrl: String, tags: List<Int>, clothesType: String, seasons: List<Int>, color: String) {
        viewModelScope.launch {
            Log.d(
                TAG,
                "registrationCloth: photoUrl : $photoUrl tags : $tags, clothesTypes : $clothesType, seasons : $seasons, color : $color"
            )
            try {
                val res = repository.registrationCloth(
                    RegistrationClothesDto(
                        photoUrl = photoUrl,
                        tags = tags,
                        clothesType = clothesType,
                        seasons = seasons,
                        color = color
                    )
                )

                if (res.isSuccessful) {
//                    _message.emit(res.body()?.responseMessage ?: "등록 성공")
                    // 등록 후 상세 페이지 이동 관련 로직
                    val newId = res.body()?.data?.clothesId ?: return@launch
                    _navigateToDetail.emit(newId)
                } else {
                    Log.d(TAG, "등록 실패 : ${res.body()?.errorMessage}")
                    _message.emit(res.body()?.errorMessage ?: "등록 실패")
                }
            } catch (e: Exception) {
                Log.e(TAG, "옷 등록 오류 발생 : ${e.message}")
            }
        }
    }

    // 옷 수정
    fun patchCloth(
        clothesId: Int,
        photoUrl: String,
        tags: List<Int>,
        clothesType: String,
        seasons: List<Int>,
        color: String
    ) {
        viewModelScope.launch {
            try {
                val res = repository.patchCloth(
                    clothesId,
                    RegistrationClothesDto(
                        photoUrl = photoUrl,
                        tags = tags,
                        clothesType = clothesType,
                        seasons = seasons,
                        color = color
                    )
                )

                if (res.isSuccessful) {
//                    _message.emit(res.body()?.responseMessage ?: "수정 성공")
                    _navigateToDetail.emit(clothesId)
                } else {
                    _message.emit(res.body()?.errorMessage ?: "수정 실패")
                }
            } catch (e: Exception) {
                _message.emit(e.message ?: "네트워크 오류")
            }
        }
    }

    fun requestClothesAlteration(photoUrl: String) {
        viewModelScope.launch {
            try {
                val res = repository.requestClothesAlteration(PhotoUrlDto(photoUrl))

                if (res.isSuccessful) {
                    val editedImage = res.body()?.data?.photoUrl
                    _imageUrl.value = editedImage
                } else {
                    _message.emit(res.body()?.errorMessage ?: "개선 실패")
                }
            } catch (e: Exception) {
                _message.emit(e.message ?: "네트워크 오류")
            }
        }
    }
}
