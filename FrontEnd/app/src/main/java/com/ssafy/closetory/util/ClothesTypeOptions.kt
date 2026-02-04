package com.ssafy.closetory.util

import android.content.Context
import android.view.View
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.ssafy.closetory.R

object ClothTypeOptions {

    val items = listOf(
        OptionItem("TOP", "상의", 1),
        OptionItem("BOTTOM", "하의", 2),
        OptionItem("OUTER", "아우터", 3),
        OptionItem("SHOES", "신발", 4),
        OptionItem("BAG", "가방", 5),
        OptionItem("ACCESSORIES", "소품류", 6)
    )

    val byEnglish: Map<String, OptionItem> =
        items.mapNotNull { item ->
            item.codeEnglish?.let { eng -> eng to item }
        }.toMap()

    fun render(sectionRoot: View, context: Context) {
        ChipUtils.renderOptionSection(sectionRoot, context, "옷 종류", items, true, false)
    }

    fun getClothTypeEnglish(sectionRoot: View): String? {
        val group = sectionRoot.findViewById<ChipGroup>(R.id.chipGroup)
        val checkedId = group.checkedChipId
        if (checkedId == View.NO_ID) return null

        val chip = group.findViewById<Chip>(checkedId)
        val code = chip.tag as? Int ?: return null
        return items.firstOrNull { it.code == code }?.codeEnglish
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
