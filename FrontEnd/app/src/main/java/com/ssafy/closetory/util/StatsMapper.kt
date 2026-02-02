package com.ssafy.closetory.util

data class StatRatioItem(val label: String, val count: Int, val ratio: Float)

fun toTopNWithOtherRatio(raw: Map<String, Int>, topN: Int = 5, otherLabel: String = "기타"): List<StatRatioItem> {
    if (raw.isEmpty()) return emptyList()

    val cleaned = raw.filterValues { it > 0 }
    val total = cleaned.values.sum()
    if (total <= 0) return emptyList()

    val sorted = cleaned.entries.sortedByDescending { it.value }
    val top = sorted.take(topN)
    val otherSum = sorted.drop(topN).sumOf { it.value }

    val items = mutableListOf<StatRatioItem>()
    items += top.map {
        StatRatioItem(
            label = it.key,
            count = it.value,
            ratio = (it.value.toFloat() / total.toFloat()) * 100f
        )
    }

    if (otherSum > 0) {
        items += StatRatioItem(
            label = otherLabel,
            count = otherSum,
            ratio = (otherSum.toFloat() / total.toFloat()) * 100f
        )
    }

    return items
}
