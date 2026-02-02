package com.ssafy.closetory.util

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.closetory.R

// 서버로 보낼 영문 코드 / 사용자에게 보여줄 한글 / 표시 색상

data class ColorItem(val codeEnglish: String, val codeKorean: String, val argb: Int)

object ColorOptions {

    private class ColorGridSpacingItemDecoration(private val spanCount: Int, private val spacingPx: Int) :
        RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) return
            val column = position % spanCount
            val half = spacingPx / 2
            outRect.left = spacingPx - column * spacingPx / spanCount
            outRect.right = (column + 1) * spacingPx / spanCount
            outRect.top = if (position < spanCount) 0 else half
            outRect.bottom = half
        }
    }

    val items = listOf(
        ColorItem("BLACK", "블랙", 0xFF000000.toInt()),
        ColorItem("WHITE", "화이트", 0xFFFFFFFF.toInt()),
        ColorItem("GRAY", "그레이", 0xFFBDBDBD.toInt()),
        ColorItem("IVORY", "아이보리", 0xFFFFF8E1.toInt()),
        ColorItem("NAVY", "네이비", 0xFF1B263B.toInt()),
        ColorItem("BLUE", "블루", 0xFF1E88E5.toInt()),
        ColorItem("GREEN", "그린", 0xFF43A047.toInt()),
        ColorItem("KHAKI", "카키", 0xFFBDB76B.toInt()),
        ColorItem("BROWN", "브라운", 0xFF8D6E63.toInt()),
        ColorItem("PINK", "핑크", 0xFFD81B60.toInt()),
        ColorItem("RED", "레드", 0xFFE53935.toInt()),
        ColorItem("ORANGE", "오렌지", 0xFFFB8C00.toInt()),
        ColorItem("YELLOW", "옐로우", 0xFFFDD835.toInt()),
        ColorItem("PURPLE", "퍼플", 0xFF8E24AA.toInt())
    )

    private val byCode = items.associateBy { it.codeEnglish }

    // 영문 코드를 한글로 변환
    fun englishToKorean(code: String?): String? {
        val key = code?.trim()?.uppercase() ?: return null
        return byCode[key]?.codeKorean
    }

    // 영문 코드를 색상 값으로 변환
    fun englishToArgb(code: String?): Int? {
        val key = code?.trim()?.uppercase() ?: return null
        return byCode[key]?.argb
    }

    fun setup(sectionRoot: View): ColorAdapter {
        // 타이틀/리사이클러뷰 바인딩
        val tv = sectionRoot.findViewById<TextView>(R.id.tvTitle)
        val rv = sectionRoot.findViewById<RecyclerView>(R.id.rvColors)

        tv.text = "색상"

        // 5열 그리드로 세로 스크롤
        rv.layoutManager = GridLayoutManager(sectionRoot.context, 5)
        rv.itemAnimator = null

        val spacingPx = (sectionRoot.resources.displayMetrics.density * 5).toInt()
        if (rv.itemDecorationCount == 0) {
            rv.addItemDecoration(ColorGridSpacingItemDecoration(5, spacingPx))
        }

        // 어댑터 연결
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

            // 기본 색상 원: fill drawable + tint
            if (item.codeEnglish == "OTHER") {
                holder.circle.setBackgroundResource(R.drawable.bg_circle_rainbow)
                holder.circle.backgroundTintList = null
            } else {
                holder.circle.setBackgroundResource(R.drawable.bg_circle_fill)
                holder.circle.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(item.argb)
            }

            // 테두리 표시
            holder.border.visibility = View.VISIBLE

            // 선택 표시
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

        // 수정 화면에서 기존 색상 반영
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
