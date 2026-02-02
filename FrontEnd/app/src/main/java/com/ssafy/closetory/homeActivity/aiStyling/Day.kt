package com.ssafy.closetory.homeActivity.aiStyling

data class Day(
    val dayText: String,
    val inMonth: Boolean,
    val year: Int,
    val month0: Int,
    val dayOfMonth: Int,
    val dayOfWeek: Int, // 1(일)~7(토)
    val isToday: Boolean,
    val topColor: Int? = null,
    val bottomColor: Int? = null
)
