package com.ssafy.closetory.util

import android.content.Context
import android.view.View

// 태그 요소와 동작은 동일하여 설명 생략

object SeasonOptions {

    val items = listOf(
        OptionItem("SPRING", "봄", 1),
        OptionItem("SUMMER", "여름", 2),
        OptionItem("FALL", "가을", 3),
        OptionItem("WINTER", "겨울", 4)
    )

    // 영문, 한글이 와도 Int형 코드를 반환하는 코드
    fun toCode(value: String?): Int? {
        val v = value?.trim().orEmpty()
        if (v.isEmpty()) return null
        v.toIntOrNull()?.let { return it }
        val upper = v.uppercase()
        codeByEnglish[upper]?.let { return it }
        return codeByKorean[v]
    }

    // 코드를 영어로
    private val codeByEnglish: Map<String, Int> =
        items.mapNotNull { item ->
            item.codeEnglish?.trim()?.let { eng -> eng to item.code }
        }.toMap()

    private val codeByKorean: Map<String, Int> =
        items.associate { it.codeKorean.trim() to it.code }

    fun render(sectionRoot: View, context: Context) {
        ChipUtils.renderOptionSection(sectionRoot, context, "계절", items, false, false)
    }

    fun getSelectedSeason(sectionRoot: View): List<Int> {
        return ChipUtils.getSelectedCodes(sectionRoot)
    }

    fun setSelectedSeason(sectionRoot: View, selected: List<Int>) {
        ChipUtils.setSelectedCodes(sectionRoot, selected)
    }
}
