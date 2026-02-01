package com.ssafy.closetory.homeActivity.closet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.ClosetResponse
import com.ssafy.closetory.dto.ClothesItemDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "ClosetViewModel_싸피"

class ClosetViewModel : ViewModel() {

    private val repository = ClosetRepository()

    private val _closetData = MutableLiveData<ClosetResponse?>()
    val closetData: LiveData<ClosetResponse?> = _closetData

    private val _message = MutableSharedFlow<String?>(replay = 0)
    val message: SharedFlow<String?> = _message

    private val _clothesData = MutableLiveData<ClothesItemDto>()
    val clothesData: LiveData<ClothesItemDto> = _clothesData

    private val _deleteSuccess = MutableSharedFlow<Boolean>(replay = 0)
    val deleteSuccess: SharedFlow<Boolean> = _deleteSuccess

    private val _recommendedClothes = MutableLiveData<List<ClothesItemDto>>()
    val recommendedClothes: LiveData<List<ClothesItemDto>> = _recommendedClothes

    private val _clothesRental = MutableSharedFlow<Boolean>(replay = 0)
    val clothesRental: SharedFlow<Boolean> = _clothesRental

    fun getClothesList(tags: List<Int>?, color: String?, seasons: List<Int>?, onlyMine: Boolean?) {
        viewModelScope.launch {
            try {
                val res = repository.getClothesList(
                    tags,
                    color,
                    seasons,
                    onlyMine
                )

                if (res.isSuccessful) { // 통신 결과 200번 때 결과
                    val body = res.body()
                    val data = body?.data

                    Log.d(TAG, "getClothesList: $data")

                    _closetData.value = data
                } else { // 통신 결과 400, 500번 때 결과
                    val body = res.body()
                    _message.emit(body?.errorMessage!!)
                }
            } catch (e: Exception) {
                _message.emit(e.message ?: "네트워크 오류")
            }
        }
    }

    fun getRecommendedClothes(clothesId: Int) {
        viewModelScope.launch {
            try {
                val res = repository.getRecommendedClothes(clothesId)

                if (res.isSuccessful) {
                    val data = res.body()?.data ?: emptyList()

                    Log.d(TAG, "추천 옷 결과 data : $data")
                    _recommendedClothes.value = data
                } else {
                    val message = res.body()?.errorMessage

                    Log.d(TAG, "추천 옷 결과 조회 실패 : $message")
                    _message.emit(message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "추천 옷 결과 조회 예외 발생 : ${e.message}")
                _message.emit("상세 조회 추천 옷 예외 발생 : ${e.message ?: "네트워크 오류"}")
            }
        }
    }

    fun getClothesDetail(clothesId: Int) {
        viewModelScope.launch {
            try {
                val res = repository.getClothesDetail(clothesId)

                if (res.isSuccessful) {
                    val data = res.body()?.data
                    _clothesData.value = data!!

                    Log.d(TAG, "옷 상세 정보 통신 결과  clothesId : ${data.clothesId}")
                    Log.d(TAG, "옷 상세 정보 통신 결과  photoUrl : ${data.photoUrl}")
                    Log.d(TAG, "옷 상세 정보 통신 결과  tags : ${data.tags}")
                    Log.d(TAG, "옷 상세 정보 통신 결과  clothesType : ${data.clothesType}")
                    Log.d(TAG, "옷 상세 정보 통신 결과  color : ${data.color}")
                    Log.d(TAG, "옷 상세 정보 통신 결과  isMine : ${data.isMine}")
                    Log.d(TAG, "옷 상세 정보 통신 결과  seasons : ${data.seasons}")

                    Log.d(TAG, "옷 상세 정보 조회 결과 : $data")
                } else {
                    val errorMessage = res.body()?.errorMessage

                    Log.d(TAG, "옷 상세 정보 통신 결과  errorMessage : $errorMessage")

                    _message.emit(errorMessage)
                }
            } catch (e: Exception) {
                _message.emit(e.message ?: "네트워크 오류")
            }
        }
    }

    fun deleteClothes(clothesId: Int) {
        viewModelScope.launch {
            try {
                val res = repository.deleteClothes(clothesId)

                if (res.isSuccessful) {
                    _deleteSuccess.emit(true)
                } else {
                    _message.emit(res.body()?.errorMessage ?: "삭제 실패")
                    _deleteSuccess.emit(false)
                }
            } catch (e: Exception) {
                _message.emit(e.message ?: "네트워크 오류")
            }
        }
    }

    fun deleteClothesRental(clothesId: Int) {
        viewModelScope.launch {
            try {
                val res = repository.deleteClothesRental(clothesId)

                if (res.isSuccessful) {
                    // false를 제공
                    _clothesRental.emit(!res.isSuccessful)
                    val message = res.body()?.responseMessage ?: "저장된 옷이 삭제 됐습니다."
                    _message.emit(message)
                } else {
                    val message = res.body()?.errorMessage!!
                    _message.emit(message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "옷 대여 취소 예외 발생 : ${e.message}")
                _message.emit(e.message ?: "네트워크 오류 발생")
            }
        }
    }

    fun postClothesRental(clothesId: Int) {
        viewModelScope.launch {
            try {
                val res = repository.postClothesRental(clothesId)

                if (res.isSuccessful) {
                    // true를 제공
                    _clothesRental.emit(res.isSuccessful)
                    val message = res.body()?.responseMessage ?: "다른 사람 옷 저장 성공"
                    _message.emit(message)
                } else {
                    val message = res.body()?.errorMessage!!
                    _message.emit(message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "옷 대여 예외 발생 : ${e.message}")
                _message.emit(e.message ?: "네트워크 오류 발생")
            }
        }
    }
}
