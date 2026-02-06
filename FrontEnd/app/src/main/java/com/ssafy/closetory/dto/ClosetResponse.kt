package com.ssafy.closetory.dto

data class ClosetResponse(
    val topClothes: List<ClothesItemDto>?,
    val bottomClothes: List<ClothesItemDto>?,
    val outerClothes: List<ClothesItemDto>?,
    val shoes: List<ClothesItemDto>?,
    val accessories: List<ClothesItemDto>?,
    val bags: List<ClothesItemDto>?
)
