package com.ssafy.closetory.dto

data class ClosetDataDto(
    val topClothes: List<ClothItemDto>,
    val bottomClothes: List<ClothItemDto>,
    val outerClothes: List<ClothItemDto>,
    val shoes: List<ClothItemDto>,
    val accessories: List<ClothItemDto>,
    val bags: List<ClothItemDto>
)
