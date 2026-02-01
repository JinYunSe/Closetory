package com.ssafy.closetory.homeActivity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.TagResponse
import kotlin.math.log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "HomeInitViewModel_싸피"
class HomeInitViewModel : ViewModel() {
    private val homeInitRepository = HomeInitRepository()

    private val _tagsList = MutableLiveData<List<TagResponse>>()
    val tagsList: LiveData<List<TagResponse>> = _tagsList
    fun getTagsList() {
        viewModelScope.launch {
            val res = homeInitRepository.getTagsList()

            if (res.isSuccessful) {
                val data = res.body()?.data
                _tagsList.value = data!!
                Log.d(TAG, "getTagsList: $data")
            } else {
                Log.d(TAG, "getTagsList: ${res.body()?.errorMessage}")
            }
        }
    }
}
