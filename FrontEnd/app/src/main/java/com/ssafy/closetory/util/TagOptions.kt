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

    // 태그 목록
    public val items = listOf(
        OptionItem("캐주얼", "CASUAL"),
        OptionItem("귀여움", "CUTE"),
        OptionItem("시크", "CHIC"),
        OptionItem("화려함", "GLAM"),
        OptionItem("밝음", "BRIGHT"),
        OptionItem("유니크", "UNIQUE"),
        OptionItem("여성스러움", "FEMININE"),
        OptionItem("남성스러움", "MASCULINE"),
        OptionItem("트렌디", "TRENDY"),
        OptionItem("빈티지", "VINTAGE"),
        OptionItem("데이트", "DATE"),
        OptionItem("출근/업무", "WORK"),
        OptionItem("일상", "DAILY"),
        OptionItem("여행", "TRAVEL"),
        OptionItem("격식 있는 자리", "FORMAL"),
        OptionItem("운동", "SPORTS")
    )

    // 태그들 UI에 그리는 메서드
    fun render(sectionRoot: View, context: Context) {
        renderChips(
            sectionRoot,
            context,
            "태그",
            items,
            false, // 단일 선택이 아님을 지정 => 다중 선택 가능
            false // 꼭 넣어야 하는 요소인지 표기하기
        )
    }

    // 태그 목록 그리기
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

        // 태그 제목
        tv.text = title

        // 화면을 왔다 갔다 할 경우 새로 UI 요소 그리기 위해서 기존 요소 제거
        group.removeAllViews()
        group.isSingleSelection = single
        group.isSelectionRequired = required

        // 선택된 태그 코드들을 저장할 목록
        val selectedTags = linkedSetOf<String>()

        // 각각의 요소들 UI에 그리는 과정
        items.forEach { item ->

            val chip = Chip(context).apply {
                // UI에 한국어 반영
                text = item.labelKorean

                // 서버로 보낼 영문
                tag = item.codeEnglish

                // 선택 가능하도록 만들기
                isCheckable = true

                // 칩간에 자동 간격 조절 끄기
                setEnsureMinTouchTargetSize(false)

                // UI 그리는 작업 메서드
                setChipDrawable(
                    ChipDrawable.createFromAttributes(
                        context,
                        null,
                        0,
                        com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice
                    )
                )
            }

            chip.setOnCheckedChangeListener { button, isChecked ->
                // 선택 <-> 해제 왔다갔다 처리

                val code = button.tag as String

                if (isChecked) {
                    // 선택됨
                    selectedTags.add(code)
                } else {
                    // 해제됨
                    selectedTags.remove(code)
                }
            }

            group.addView(chip)
        }
    }

    // 체크된 대상이 true인 대상만 골라서 반환
    public fun getSelectedTag(sectionRoot: View): List<String> {
        // 태그 그룹 가져오기
        val group = sectionRoot.findViewById<ChipGroup>(R.id.chipGroup)
        val result = mutableListOf<String>()

        for (i in 0 until group.childCount) {
            // chip의 요소가 아니면 무시
            val chip = group.getChildAt(i) as? Chip ?: continue
            // 선택된 태그 요소의 영문을 String 타입으로 변경 및 리스트에 담기
            if (chip.isChecked) result.add(chip.tag as String)
        }

        return result
    }
}
