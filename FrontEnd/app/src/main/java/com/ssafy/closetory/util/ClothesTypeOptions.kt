package com.ssafy.closetory.util

import android.content.Context
import android.view.View
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.ssafy.closetory.R

object ClothTypeOptions {

    data class ClothTypeItem(val codeKorean: String, val code: Int, val english: String)

    val items = listOf(
        ClothTypeItem("상의", 1, "TOP"),
        ClothTypeItem("하의", 2, "BOTTOM"),
        ClothTypeItem("아우터", 3, "OUTER"),
        ClothTypeItem("신발", 4, "SHOES"),
        ClothTypeItem("가방", 5, "BAG"),
        ClothTypeItem("악세서리", 6, "ACCESSORY")
    )

    val byEnglish: Map<String, ClothTypeItem> = items.associateBy { it.english }

    fun render(sectionRoot: View, context: Context) {
        val tv = sectionRoot.findViewById<TextView>(R.id.tvTitle)
        val group = sectionRoot.findViewById<ChipGroup>(R.id.chipGroup)

        tv.text = "옷 종류"
        group.removeAllViews()
        group.isSingleSelection = true
        group.isSelectionRequired = false

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

                chipBackgroundColor = context.getColorStateList(R.color.chip_bg_selector)
                setTextColor(context.getColorStateList(R.color.chip_text_selector))
            }
            group.addView(chip)
        }
    }

    fun getClothTypeEnglish(sectionRoot: View): String? {
        val group = sectionRoot.findViewById<ChipGroup>(R.id.chipGroup)
        val checkedId = group.checkedChipId
        if (checkedId == View.NO_ID) return null

        val chip = group.findViewById<Chip>(checkedId)
        val code = chip.tag as? Int ?: return null
        return items.firstOrNull { it.code == code }?.english
    }

    fun setClothType(sectionRoot: View, clothType: Int) {
        val group = sectionRoot.findViewById<ChipGroup>(R.id.chipGroup)
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as? Chip ?: continue
            val code = chip.tag as? Int ?: continue
            if (code == clothType) {
                chip.isChecked = true
                return
            }
        }
        group.clearCheck()
    }

    fun setClothTypeByEnglish(sectionRoot: View, english: String) {
        val code = byEnglish[english]?.code
        if (code == null) {
            sectionRoot.findViewById<ChipGroup>(R.id.chipGroup).clearCheck()
            return
        }
        setClothType(sectionRoot, code)
    }

    fun englishToKorean(english: String?): String {
        if (english.isNullOrBlank()) return ""
        return byEnglish[english]?.codeKorean ?: english
    }
}
