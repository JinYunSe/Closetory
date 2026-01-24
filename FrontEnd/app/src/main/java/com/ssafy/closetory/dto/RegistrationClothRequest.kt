package com.ssafy.closetory.dto

import android.net.Uri

data class RegistrationClothRequest(
    val photoUrl: Uri,
    val tags: List<Int>,
    val clothesTypes: Int,
    val seasons: List<Int>,
    val color: String
)
