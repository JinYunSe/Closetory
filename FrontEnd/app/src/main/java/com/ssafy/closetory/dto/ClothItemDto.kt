package com.ssafy.closetory.dto

data class ClothItemDto(
    val clothesId: Int,
    val photoUrl: String,
    val tags: List<String>?,
    val clothesType: String?,
    val seasons: List<String>?,
    val color: String?,
    val isMine: Boolean?
)
