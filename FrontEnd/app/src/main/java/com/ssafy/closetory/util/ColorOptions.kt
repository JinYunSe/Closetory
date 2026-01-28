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

        // 현재 선택한 대상 index 저장할 변수
        private var selectedPos = RecyclerView.NO_POSITION

        // 리사이클러 뷰 요소가 반영될 XML의 요소들 가져오기
        inner class ViewHold(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val circle: View = itemView.findViewById(R.id.v_circle)
            val stroke: View = itemView.findViewById(R.id.v_stroke)
        }

        // 리사이클러 뷰 요소가 반영될 XML 가져오기
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHold {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_color_circle, parent, false)

            return ViewHold(view)
        }

        // 리사이클러 뷰 내부 요소 개수
        override fun getItemCount(): Int = items.size

        // 리사이클러 뷰 내부 요소 동작 시키기
        @SuppressLint("RecyclerView")
        override fun onBindViewHolder(holder: ViewHold, position: Int) {
            // 선택한 요소
            val item = items[position]

            // 선택한 대상 색상 가져오기
            holder.circle.background.mutate().setTint(item.argb)

            // 현재 선택된 대상에는 테두리 주기
            /*
            Kotlin에는 삼항 연산자가 없는 대신 아래와 같이 작성하면
            if - else 문의 마지막 코드를 return 해줍니다
            => 그 결과, 선택된 대상은 테두리가 보이게 하고, 안 된 대상은 안 보이게 만든다.
             */
            holder.stroke.visibility = if (position == selectedPos) View.VISIBLE else View.GONE

            // 요소를 선택할 경우
            holder.itemView.setOnClickListener {
                // 이전 요소와 같은 요소 클릭하면 선택 해제로 변경
                val clickedPos = holder.bindingAdapterPosition
                if (clickedPos == RecyclerView.NO_POSITION) return@setOnClickListener

                // 이전 선택 대상
                val prev = selectedPos

                // 같은 아이템을 다시 누르면 선택 해제
                selectedPos = if (prev == clickedPos) RecyclerView.NO_POSITION else clickedPos

                // 이전 선택 대상 UI 화면 바꾸기(테두리 빼기)
                notifyItemChanged(prev)

                // 지금 선택 대상이 같은 대상이 아니면 UI 화면 바꾸기(테두리 넣기)
                // 같은 대상이었으면 태두리 빼기
                if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev)
                if (selectedPos != RecyclerView.NO_POSITION) notifyItemChanged(selectedPos)
            }
        }

        // 선택 없으면 null
        fun getSelectedColor(): String? {
            if (selectedPos == RecyclerView.NO_POSITION) return null
            return items[selectedPos].codeEnglish
        }

        fun setSelectedColor(codeEnglish: String) {
            val key = codeEnglish.trim().uppercase()
            val newPos = items.indexOfFirst { it.codeEnglish.uppercase() == key }

            val prev = selectedPos
            selectedPos = if (newPos >= 0) newPos else RecyclerView.NO_POSITION

            if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev)
            if (selectedPos != RecyclerView.NO_POSITION) notifyItemChanged(selectedPos)
        }
    }
}
