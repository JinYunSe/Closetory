package com.ssafy.closetory.util

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.closetory.R

//                      서버로 보낼 영문       사용자에게 보여줄 한국어       UI에 보여줄 색상
data class ColorItem(val codeEnglish: String, val codeKorean: String, val argb: Int)

object ColorOptions {

    val items = listOf(
        ColorItem("RED", "빨간색", 0xFFE53935.toInt()),
        ColorItem("ORANGE", "주황색", 0xFFFB8C00.toInt()),
        ColorItem("YELLOW", "노란색", 0xFFFDD835.toInt()),
        ColorItem("GREEN", "초록색", 0xFF43A047.toInt()),
        ColorItem("BLUE", "파란색", 0xFF1E88E5.toInt()),
        ColorItem("PURPLE", "보라색", 0xFF8E24AA.toInt()),
        ColorItem("PINK", "핑크색", 0xFFD81B60.toInt()),
        ColorItem("BLACK", "검은색", 0xFF000000.toInt()),
        ColorItem("WHITE", "흰색", 0xFFFFFFFF.toInt()),
        ColorItem("BEIGE", "베이지", 0xFFD7CCC8.toInt()),
        ColorItem("GRAY", "회색", 0xFFBDBDBD.toInt())
    )

    private val byCode = items.associateBy { it.codeEnglish }

    // 영문을 한국어로 변형
    fun englishToKorean(code: String?): String? {
        val key = code?.trim()?.uppercase() ?: return null
        return byCode[key]?.codeKorean
    }

    // 영문을 색상으로 변형
    fun englishToArgb(code: String?): Int? {
        val key = code?.trim()?.uppercase() ?: return null
        return byCode[key]?.argb
    }

    fun setup(sectionRoot: View): ColorAdapter {
        // 텍스트와 리사이클러 뷰 가져오기
        val tv = sectionRoot.findViewById<TextView>(R.id.tvTitle)
        val rv = sectionRoot.findViewById<RecyclerView>(R.id.rvColors)

        tv.text = "색상"

        // 리사이클러 뷰 형식을 가로 형식 스크롤로 지정
        rv.layoutManager = LinearLayoutManager(sectionRoot.context, LinearLayoutManager.HORIZONTAL, false)
        rv.itemAnimator = null

        // 리사이클러 뷰의 요소를 가질 어뎁터 등록
        val adapter = ColorAdapter(items)
        rv.adapter = adapter

        return adapter
    }

    class ColorAdapter(private val items: List<ColorItem>) : RecyclerView.Adapter<ColorAdapter.ViewHold>() {

        private var selectedPos = RecyclerView.NO_POSITION

        inner class ViewHold(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val circle: View = itemView.findViewById(R.id.v_circle)
            val border: View = itemView.findViewById(R.id.v_border)
            val stroke: View = itemView.findViewById(R.id.v_stroke)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHold {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_color_circle, parent, false)
            return ViewHold(view)
        }

        override fun getItemCount(): Int = items.size

        @SuppressLint("RecyclerView")
        override fun onBindViewHolder(holder: ViewHold, position: Int) {
            val item = items[position]

            // 내부 채움: 테두리 없는 drawable + tint
            holder.circle.setBackgroundResource(R.drawable.bg_circle_fill)
            holder.circle.backgroundTintList =
                android.content.res.ColorStateList.valueOf(item.argb)

            // 검은 테두리는 항상 보이게(이건 tint 안 먹음)
            holder.border.visibility = View.VISIBLE

            // 선택 링은 선택된 경우만
            holder.stroke.visibility = if (position == selectedPos) View.VISIBLE else View.GONE

            holder.itemView.setOnClickListener {
                val clickedPos = holder.bindingAdapterPosition
                if (clickedPos == RecyclerView.NO_POSITION) return@setOnClickListener

                val prev = selectedPos
                selectedPos = if (prev == clickedPos) RecyclerView.NO_POSITION else clickedPos

                if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev)
                if (selectedPos != RecyclerView.NO_POSITION) notifyItemChanged(selectedPos)
            }
        }

        fun getSelectedColor(): String? {
            if (selectedPos == RecyclerView.NO_POSITION) return null
            return items[selectedPos].codeEnglish
        }

        // 수정 화면에서 기존 값 반영용(필요하면 Registration에서 호출)
        fun setSelectedColor(codeEnglish: String?) {
            val key = codeEnglish?.trim()?.uppercase() ?: return
            val idx = items.indexOfFirst { it.codeEnglish.trim().uppercase() == key }
            val prev = selectedPos
            selectedPos = idx
            if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev)
            if (selectedPos != RecyclerView.NO_POSITION) notifyItemChanged(selectedPos)
        }
    }
}
