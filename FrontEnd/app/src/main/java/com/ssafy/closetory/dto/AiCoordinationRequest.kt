package com.ssafy.closetory.dto

data class AiCoordinationDto(
    val date: String,
    val isPersonalized: Boolean,
    val ownedItemsOnly: Boolean,
    val items: List<AiClothDto>
)
