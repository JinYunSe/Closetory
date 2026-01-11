package com.ssafy.closetory.homeActivity.styling

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StylingViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is styling Fragment"
    }
    val text: LiveData<String> = _text
}
