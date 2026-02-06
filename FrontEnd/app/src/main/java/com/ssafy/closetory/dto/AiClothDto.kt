package com.ssafy.closetory.dto

data class AiClothDto(
    val clothesId: Int,
    val clothesType: String, // "TOP", "BOTTOM", "SHOES", "OUTER", "ACCESSORIES", "BAG"
    val photoUrl: String
)
