package com.ssafy.closetory.homeActivity.mypage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.CodyRepositoryResponse
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.dto.StatisticsResponse
import com.ssafy.closetory.dto.Top3ClothesResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

private const val TAG = "MyPageViewModel_싸피"
class MyPageViewModel : ViewModel() {
    private val _recentCody = MutableLiveData<List<CodyRepositoryResponse>>()
    val recentCody: LiveData<List<CodyRepositoryResponse>> = _recentCody

    private val repository = MyPageRepository()

    private val _userProfile = MutableSharedFlow<EditProfileInfoResponse>()
    val userProfile: SharedFlow<EditProfileInfoResponse> = _userProfile

    private var cachedUserProfile: EditProfileInfoResponse? = null

    fun getCachedUserProfile(): EditProfileInfoResponse? = cachedUserProfile

    private val _passwordVerified = MutableSharedFlow<Boolean>()
    val passwordVerified = _passwordVerified.asSharedFlow()

    private val _logoutSuccess = MutableSharedFlow<Boolean>(replay = 0)
    val logoutSuccess: SharedFlow<Boolean> = _logoutSuccess

    private val _message = MutableSharedFlow<String>(replay = 0)
    val message: SharedFlow<String> = _message

    private val _tagsStatistics = MutableLiveData<List<StatisticsResponse>>()
    val tagsStatistics: LiveData<List<StatisticsResponse>> = _tagsStatistics

    private val _colorStatistics = MutableLiveData<List<StatisticsResponse>>()
    val colorStatistics: LiveData<List<StatisticsResponse>> = _colorStatistics

    private val _top3Clothes = MutableLiveData<List<Top3ClothesResponse>>()
    val top3Clothes: LiveData<List<Top3ClothesResponse>> = _top3Clothes

    fun loadUserProfile(userId: Int) {
        viewModelScope.launch {
            try {
                val res = repository.getUserProfile(userId)

                if (res.isSuccessful) {
                    val data = res.body()?.data
                    if (data != null) {
                        cachedUserProfile = data
                        ApplicationClass.sharedPreferences.putBodyPhotoUrl(data.bodyPhotoUrl)
                        _userProfile.emit(data)
                    } else {
                        _message.emit("회원정보를 불러오지 못했습니다.")
                    }
                } else {
                    _message.emit(res.body()?.errorMessage ?: "회원정보 조회 실패")
                }
            } catch (e: Exception) {
                _message.emit("회원정보 조회 실패")
            }
        }
    }

    fun checkPassword(password: String) {
        val userId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID) ?: return

        viewModelScope.launch {
            try {
                val res = repository.checkPassword(userId, password)

                if (res.httpStatusCode == 200) {
                    _passwordVerified.emit(true)
                    _message.emit(res.responseMessage ?: "비밀번호 확인 완료")
                } else {
                    _passwordVerified.emit(false)
                    _message.emit(res.errorMessage ?: "비밀번호가 일치하지 않습니다.")
                }
            } catch (e: Exception) {
                Log.d(TAG, "checkPassword 예외 : ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val res = repository.logout()

                if (res.isSuccessful) {
                    val body = res.body()
                    _logoutSuccess.emit(true)
                    _message.emit(body?.responseMessage ?: "로그아웃 성공")

                    ApplicationClass.authManager.clearToken()
                    ApplicationClass.sharedPreferences.clearUserId(ApplicationClass.USERID)
                } else {
                    _logoutSuccess.emit(false)
                    _message.emit(res.body()?.errorMessage ?: "로그아웃 실패")
                }
            } catch (e: Exception) {
                _logoutSuccess.emit(false)
                _message.emit("로그아웃 실패")
            }
        }
    }

    fun getTagsStatistics(userId: Int) {
        viewModelScope.launch {
            try {
                val res = repository.getTagsStatistics(userId)

                if (res.isSuccessful) {
                    _tagsStatistics.value = res.body()?.data ?: emptyList()
                } else {
                    _tagsStatistics.value = emptyList()
                    _message.emit(res.body()?.errorMessage ?: "태그 통계 조회 실패")
                }
            } catch (e: Exception) {
                _tagsStatistics.value = emptyList()
                _message.emit("태그 통계 조회 실패")
            }
        }
    }

    fun getColorsStatistics(userId: Int) {
        viewModelScope.launch {
            try {
                val res = repository.getColorsStatistics(userId)

                if (res.isSuccessful) {
                    _colorStatistics.value = res.body()?.data ?: emptyList()
                } else {
                    _colorStatistics.value = emptyList()
                    _message.emit(res.body()?.errorMessage ?: "색상 통계 조회 실패")
                }
            } catch (e: Exception) {
                _colorStatistics.value = emptyList()
                _message.emit("색상 통계 조회 실패")
            }
        }
    }

    fun getRecentCody() {
        viewModelScope.launch {
            try {
                val res = repository.getRecentCody()

                if (res.isSuccessful) {
                    val data = res.body()?.data ?: emptyList()

                    val recentThree = data
                        .filter { !it.date.isNullOrBlank() }
                        .sortedByDescending { it.date }
                        .take(3)

                    _recentCody.value = recentThree
                } else {
                    val message = res.body()?.errorMessage ?: "최근 코디 조회 실패"
                    _recentCody.value = emptyList()
                    _message.emit(message)
                }
            } catch (e: Exception) {
                _recentCody.value = emptyList()
                _message.emit("최근 코디 조회 실패")
            }
        }
    }

    fun getTop3Clothes(userId: Int) {
        viewModelScope.launch {
            try {
                val res = repository.getTop3Clothes(userId)
                if (res.isSuccessful) {
                    _top3Clothes.value = res.body()?.data ?: emptyList()
                } else {
                    _top3Clothes.value = emptyList()
                    _message.emit(res.body()?.errorMessage ?: "Top3 조회 실패")
                }
            } catch (e: Exception) {
                _top3Clothes.value = emptyList()
                _message.emit("Top3 조회 실패")
            }
        }
    }
}
