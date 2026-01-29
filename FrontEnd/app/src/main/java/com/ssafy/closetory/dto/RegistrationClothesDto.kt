package com.ssafy.closetory.dto

data class RegistrationClothesDto(
    val photoUrl: String,
    val tags: List<Int>,
    val clothesType: String,
    val seasons: List<Int>,
    val color: String
)
