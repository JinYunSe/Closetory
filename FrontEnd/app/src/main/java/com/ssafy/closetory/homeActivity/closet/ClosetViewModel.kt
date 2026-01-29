package com.ssafy.closetory.homeActivity.closet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.ClosetResponse
import com.ssafy.closetory.dto.ClothesItemDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
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
}
