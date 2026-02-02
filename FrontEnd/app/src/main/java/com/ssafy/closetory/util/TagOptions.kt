package com.ssafy.closetory.util

import android.content.Context
import android.view.View
import com.ssafy.closetory.dto.TagResponse

// 태그 요소
object TagOptions {

    @Volatile
    var items: List<OptionItem> = emptyList()

    @Volatile
    private var codeByKorean: Map<String, Int> = emptyMap()

    fun setTags(tags: List<TagResponse>) {
        val mapped = tags
            .map { OptionItem(null, it.tagName, it.tagId) }

        items = mapped
        codeByKorean = mapped.associate { it.codeKorean.trim() to it.code }
    }

    fun isReady(): Boolean = items.isNotEmpty()

    fun toCode(value: String?): Int? {
        val v = value?.trim().orEmpty()
        if (v.isEmpty()) return null
        return v.toIntOrNull() ?: codeByKorean[v]
    }

    fun render(sectionRoot: View, context: Context) {
        if (!isReady()) return

        ChipUtils.renderOptionSection(
            sectionRoot = sectionRoot,
            context = context,
            title = "태그",
            items = items,
            single = false,
            required = false
        )
    }

    fun getSelectedTag(sectionRoot: View): List<Int> = ChipUtils.getSelectedCodes(sectionRoot)

    fun setSelectedTag(sectionRoot: View, selected: List<Int>) {
        ChipUtils.setSelectedCodes(sectionRoot, selected)
    }
}
