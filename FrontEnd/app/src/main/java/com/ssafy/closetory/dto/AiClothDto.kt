package com.ssafy.closetory.dto

data class AiClothDto(
    val clothId: Int,
    val clothImage: String,
    val tags: List<String>,
    val clothTypes: String,
    val seasons: List<String>,
    val color: String,
    val like: Boolean,
    val ownedCloth: Boolean
)
