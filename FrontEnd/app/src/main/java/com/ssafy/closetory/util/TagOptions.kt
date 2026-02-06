package com.ssafy.closetory.util

import android.content.Context
import android.view.View
import androidx.core.graphics.toColorInt
import com.ssafy.closetory.dto.TagResponse
import kotlin.math.abs

// 태그 요소
object TagOptions {

    @Volatile
    var items: List<OptionItem> = emptyList()

    @Volatile
    private var codeByKorean: Map<String, Int> = emptyMap()

    private val PIE_16_COLORS: List<Int> = listOf(
        "#0D47A1".toColorInt(),
        "#1976D2".toColorInt(),
        "#42A5F5".toColorInt(),
        "#90CAF9".toColorInt(),
        "#E3F2FD".toColorInt()
    )

    // 태그명 -> 색상
    fun colorForTag(tag: String): Int {
        if (tag.isBlank()) return "#9E9E9E".toColorInt()
        val idx = abs(tag.trim().hashCode()) % PIE_16_COLORS.size
        return PIE_16_COLORS[idx]
    }

    // 차트에서 항목 개수만큼 그냥 순서대로 쓰고 싶을 때
    fun pieColors(size: Int): List<Int> = PIE_16_COLORS.take(size.coerceAtMost(PIE_16_COLORS.size))

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
