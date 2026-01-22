package com.ssafy.closetory.util

import android.content.Context
import android.view.View
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.ssafy.closetory.R

// 태그 요소와 동작은 동일하여 설명 생략

object SeasonOptions {

    val items = listOf(
        OptionItem("봄", 1),
        OptionItem("여름", 2),
        OptionItem("가을", 3),
        OptionItem("겨울", 4)
    )

    // UI에 해당 요소들 그리는 메서드
    fun render(sectionRoot: View, context: Context) {
        renderChips(
            sectionRoot,
            context,
            "계절",
            items,
            false, // 여러개 선택 가능함을 의미
            false // 필수 요소가 아님(선택하지 않아도 됨)
        )
    }

    private fun renderChips(
        sectionRoot: View,
        context: Context,
        title: String,
        items: List<OptionItem>,
        single: Boolean,
        required: Boolean
    ) {
        val tv = sectionRoot.findViewById<TextView>(R.id.tvTitle)
        val group = sectionRoot.findViewById<ChipGroup>(R.id.chipGroup)

        tv.text = title

        group.removeAllViews()
        group.isSingleSelection = single
        group.isSelectionRequired = required

        items.forEach { item ->
            val chip = Chip(context).apply {
                text = item.labelKorean
                tag = item.code
                isCheckable = true

                // 칩간에 자동 간격 조절 끄기
                setEnsureMinTouchTargetSize(false)

                setChipDrawable(
                    ChipDrawable.createFromAttributes(
                        context,
                        null,
                        0,
                        com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice
                    )
                )
            }

            group.addView(chip)
        }
    }

    public fun getSelectedSeason(sectionRoot: View): List<Int> {
        val group = sectionRoot.findViewById<ChipGroup>(R.id.chipGroup)
        val result = mutableListOf<Int>()

        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as? Chip ?: continue
            // 선택된 항목에 대한 영문 코드 리스트에 담기
            if (chip.isChecked) result.add(chip.tag as Int)
        }

        return result
    }
}
