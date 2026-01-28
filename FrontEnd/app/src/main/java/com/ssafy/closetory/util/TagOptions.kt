package com.ssafy.closetory.util

import android.content.Context
import android.view.View
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.ssafy.closetory.R

// kotlin에서 object는 싱글톤 class를 의미 합니다.

// 태그 요소
object TagOptions {

    val items = listOf(
        OptionItem(null, "캐주얼", 1),
        OptionItem(null, "귀여움", 2),
        OptionItem(null, "시크", 3),
        OptionItem(null, "화려함", 4),
        OptionItem(null, "밝음", 5),
        OptionItem(null, "유니크", 6),
        OptionItem(null, "여성스러움", 7),
        OptionItem(null, "남성스러움", 8),
        OptionItem(null, "트렌디", 9),
        OptionItem(null, "빈티지", 10),
        OptionItem(null, "데이트", 11),
        OptionItem(null, "출근/업무", 12),
        OptionItem(null, "일상", 13),
        OptionItem(null, "여행", 14),
        OptionItem(null, "격식 있는 자리", 15),
        OptionItem(null, "운동", 16)
    )

    fun render(sectionRoot: View, context: Context) {
        renderChips(sectionRoot, context, "태그", items, false, false)
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

                setChipDrawable(
                    ChipDrawable.createFromAttributes(
                        context,
                        null,
                        0,
                        com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice
                    )
                )

                chipBackgroundColor =
                    context.getColorStateList(R.color.chip_bg_selector)
                setTextColor(
                    context.getColorStateList(R.color.chip_text_selector)
                )
            }
            group.addView(chip)
        }
    }

    fun getSelectedTag(sectionRoot: View): List<Int> {
        val group = sectionRoot.findViewById<ChipGroup>(R.id.chipGroup)
        return (0 until group.childCount)
            .mapNotNull { group.getChildAt(it) as? Chip }
            .filter { it.isChecked }
            .map { it.tag as Int }
    }

    fun setSelectedTag(sectionRoot: View, selected: List<Int>) {
        val group = sectionRoot.findViewById<ChipGroup>(R.id.chipGroup)
        val selectedSet = selected.toSet()

        (0 until group.childCount)
            .mapNotNull { group.getChildAt(it) as? Chip }
            .forEach { chip ->
                val code = chip.tag as? Int ?: return@forEach
                chip.isChecked = selectedSet.contains(code)
            }
    }
}
