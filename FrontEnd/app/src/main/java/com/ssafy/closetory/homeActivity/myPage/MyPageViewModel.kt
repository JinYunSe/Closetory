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

    // ✅ 코디 히스토리(Top3 착용 옷) LiveData 추가
    private val _top3Clothes = MutableLiveData<List<Top3ClothesResponse>>()
    val top3Clothes: LiveData<List<Top3ClothesResponse>> = _top3Clothes

    fun loadUserProfile(userId: Int) {
        Log.d(TAG, "loadUserProfile: ViewModel_loadUserProfile 실행")
        viewModelScope.launch {
            try {
                val res = repository.getUserProfile(userId)

                Log.d(TAG, "getUserProfile code=${res.code()} body=${res.body()} err=${res.errorBody()?.string()}")

                if (res.isSuccessful) {
                    val data = res.body()?.data
                    if (data != null) {
                        _userProfile.emit(data)
                    } else {
                        _message.emit("회원정보를 불러오지 못했습니다.")
                    }
                } else {
                    _message.emit(res.body()?.errorMessage ?: "회원정보 조회 실패")
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadUserProfile error", e)
            }
        }
    }

    fun checkPassword(password: String) {
        val userId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID) ?: return
        Log.d(TAG, "checkPassword: userId : $userId")

        viewModelScope.launch {
            val res = repository.checkPassword(userId, password)

            Log.d(TAG, "checkPassword 동작 확인: $res")

            Log.d(TAG, "httpStatus: ${res.httpStatusCode}")
            Log.d(TAG, "responseMessage: ${res.responseMessage}")
            Log.d(TAG, "errorMessage: ${res.errorMessage}")
            Log.d(TAG, "data: ${res.data}")

            if (res.httpStatusCode == 200) {
                _passwordVerified.emit(true)
                _message.emit(res.responseMessage ?: "확인되었습니다.")
            } else {
                _passwordVerified.emit(false)
                _message.emit(res.errorMessage ?: "비밀번호가 올바르지 않습니다.")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val res = repository.logout()

                Log.d("DEBUG", "################")
                Log.d("LOGOUT_FLOW", "response body = ${res.body()}")
                Log.d("DEBUG", "################")

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
                Log.e("LOGOUT_FLOW", "logout() 예외 발생 ${e.message}", e)
                _logoutSuccess.emit(false)
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
                Log.e(TAG, "태그 통계 예외 발생 : ${e.message}")
                _tagsStatistics.value = emptyList()
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
                Log.e(TAG, "색상 통계 예외 발생 : ${e.message}")
                _colorStatistics.value = emptyList()
            }
        }
    }

    fun getRecentCody() {
        viewModelScope.launch {
            try {
                val res = repository.getRecentCody()

                if (res.isSuccessful) {
                    val data = res.body()?.data ?: emptyList()

                    // 날짜 있는 것만 필터링 + 날짜 기준 내림차순 정렬 + 최근 3개만
                    val recentThree = data
                        .filter { !it.date.isNullOrBlank() }
                        .sortedByDescending { it.date }
                        .take(3)

                    Log.d(TAG, "최근 코디 조회 성공: ${recentThree.size}개")
                    _recentCody.value = recentThree
                } else {
                    val message = res.body()?.errorMessage ?: "최근 코디 조회 실패"
                    _recentCody.value = emptyList()
                    _message.emit(message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "최근 코디 조회 예외 발생: ${e.message}", e)
                _recentCody.value = emptyList()
            }
        }
    }

    // ✅ Top3 착용 옷 조회 추가
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
                Log.e(TAG, "getTop3Clothes 예외 발생 : ${e.message}", e)
                _top3Clothes.value = emptyList()
            }
        }
    }
}
