package com.ssafy.closetory.util

import android.content.Context
import android.view.View
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.ssafy.closetory.R

object ChipUtils {

    fun createChoiceChip(
        context: Context,
        text: CharSequence,
        tag: Any? = null,
        checkable: Boolean = true,
        checked: Boolean = false,
        clickable: Boolean = true,
        focusable: Boolean = false
    ): Chip {
        return Chip(context).apply {
            this.text = text
            this.tag = tag
            isCheckable = checkable
            isChecked = checked
            isClickable = clickable
            isFocusable = focusable
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
    }

    fun renderOptionSection(
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
            val chip = createChoiceChip(
                context = context,
                text = item.codeKorean,
                tag = item.code,
                checkable = true
            )
            group.addView(chip)
        }
    }

    fun getSelectedCodes(sectionRoot: View): List<Int> {
        val group = sectionRoot.findViewById<ChipGroup>(R.id.chipGroup)
        return (0 until group.childCount)
            .mapNotNull { group.getChildAt(it) as? Chip }
            .filter { it.isChecked }
            .mapNotNull { it.tag as? Int }
    }

    fun setSelectedCodes(sectionRoot: View, selected: Collection<Int>) {
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
