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
        group.isSelectionRequired = false

        // 이전에 선택한 대상
        var lastCheckedId = View.NO_ID

        items.forEach { item ->
            val chip = Chip(context).apply {
                id = View.generateViewId()

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

            chip.setOnClickListener {
                // 이전 선택 대상이 같은지 확인
                val wasSelected = (lastCheckedId == chip.id)

                // 같을 경우 선택 해제, 다를 경우 선택으로 지정
                if (wasSelected) {
                    group.clearCheck()
                    lastCheckedId = View.NO_ID
                } else {
                    group.check(chip.id)
                    lastCheckedId = chip.id
                }
            }
            group.addView(chip)
        }
    }

    // 선택 안 하면 null
    public fun getClothType(sectionRoot: View): String? {
        val group = sectionRoot.findViewById<ChipGroup>(R.id.chipGroup)
        val checkedId = group.checkedChipId
        if (checkedId == View.NO_ID) return null
        val chip = group.findViewById<Chip>(checkedId)
        return chip.tag as? String
    }
}
