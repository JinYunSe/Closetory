package com.ssafy.closetory.dto

data class RegistrationClothRequest(
    val photoUrl: String,
    val tags: List<Int>,
    val clothesTypes: Int,
    val seasons: List<Int>,
    val color: String
)
