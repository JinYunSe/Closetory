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
        OptionItem("TOP", "상의", 1),
        OptionItem("BOTTOM", "하의", 2),
        OptionItem("ACCESSORIES", "악세사리", 3),
        OptionItem("BAG", "가방", 4),
        OptionItem("OUTER", "아우터", 5),
        OptionItem("SHOES", "신발", 6)
    )

    private val byEnglish = items.associateBy { it.codeEnglish }

    fun render(sectionRoot: View, context: Context) {
        val tv = sectionRoot.findViewById<TextView>(R.id.tvTitle)
        val group = sectionRoot.findViewById<ChipGroup>(R.id.chipGroup)

        tv.text = "옷 종류"
        group.removeAllViews()
        group.isSingleSelection = true
        group.isSelectionRequired = false

        var lastCheckedId = View.NO_ID

        items.forEach { item ->
            val chip = Chip(context).apply {
                id = View.generateViewId()
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

            chip.setOnClickListener {
                if (lastCheckedId == chip.id) {
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

    fun getClothType(sectionRoot: View): Int? {
        val group = sectionRoot.findViewById<ChipGroup>(R.id.chipGroup)
        val id = group.checkedChipId
        return if (id == View.NO_ID) {
            null
        } else {
            (group.findViewById<Chip>(id).tag as Int)
        }
    }

    fun englishToKorean(code: String?): String? = byEnglish[code]?.codeKorean
}
