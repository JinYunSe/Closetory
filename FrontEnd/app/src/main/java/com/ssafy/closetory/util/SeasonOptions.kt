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
        OptionItem(null, "봄", 1),
        OptionItem(null, "여름", 2),
        OptionItem(null, "가을", 3),
        OptionItem(null, "겨울", 4)
    )

    fun render(sectionRoot: View, context: Context) {
        renderChips(sectionRoot, context, "계절", items, false, false)
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
                text = item.codeKorean
                tag = item.code
                isCheckable = true
                setEnsureMinTouchTargetSize(false)

                // 1️기본 스타일
                setChipDrawable(
                    ChipDrawable.createFromAttributes(
                        context,
                        null,
                        0,
                        com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice
                    )
                )

                // 2️상태 색상
                chipBackgroundColor =
                    context.getColorStateList(R.color.chip_bg_selector)
                setTextColor(
                    context.getColorStateList(R.color.chip_text_selector)
                )
            }
            group.addView(chip)
        }
    }

    fun getSelectedSeason(sectionRoot: View): List<Int> {
        val group = sectionRoot.findViewById<ChipGroup>(R.id.chipGroup)
        return (0 until group.childCount)
            .mapNotNull { group.getChildAt(it) as? Chip }
            .filter { it.isChecked }
            .map { it.tag as Int }
    }
}
