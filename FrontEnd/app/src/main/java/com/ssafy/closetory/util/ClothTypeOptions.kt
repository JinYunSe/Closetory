package com.ssafy.closetory.util

import android.content.Context
import android.view.View
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.ssafy.closetory.R

object ClothTypeOptions {

    val items = listOf(
        OptionItem("상의", "TOP"),
        OptionItem("하의", "BOTTOM"),
        OptionItem("악세사리", "ACCESSORY"),
        OptionItem("가방", "BAG"),
        OptionItem("원피스", "ONEPIECE"),
        OptionItem("아우터", "OUTER")
    )

    fun render(sectionRoot: View, context: Context) {
        val tv = sectionRoot.findViewById<TextView>(R.id.tvTitle)
        val group = sectionRoot.findViewById<ChipGroup>(R.id.chipGroup)

        tv.text = "옷 종류"

        group.removeAllViews()
        group.isSingleSelection = true
        group.isSelectionRequired = true

        items.forEach { item ->
            val chip = Chip(context).apply {
                text = item.labelKorean
                tag = item.codeEnglish
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

    fun getSelectedCode(sectionRoot: View): String {
        val group = sectionRoot.findViewById<ChipGroup>(R.id.chipGroup)
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as? Chip ?: continue
            if (chip.isChecked) return chip.tag as String
        }
        throw IllegalStateException("옷 종류가 선택되지 않았습니다.")
    }
}
