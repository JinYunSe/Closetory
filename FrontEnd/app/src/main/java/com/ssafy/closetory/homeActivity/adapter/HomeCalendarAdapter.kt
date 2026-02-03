// package com.ssafy.closetory.homeActivity.adapter
//
// import android.graphics.Color
// import android.view.LayoutInflater
// import android.view.View
// import android.view.ViewGroup
// import androidx.core.content.ContextCompat
// import androidx.recyclerview.widget.RecyclerView
// import com.ssafy.closetory.R
// import com.ssafy.closetory.homeActivity.aiStyling.Day
// import com.ssafy.closetory.util.StrokeTextView
//
// class HomeCalendarAdapter(
//    private var items: List<Day>,
//    private val onClick: (Day) -> Unit,
//    private val colorProvider: (Day) -> Pair<Int?, Int?>,
//    private val isBlocked: (Day) -> Boolean // ✅ 추가
// ) : RecyclerView.Adapter<HomeCalendarAdapter.VH>() {
//
//    private var selectedKey: String? = null
//
//    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val tv: StrokeTextView = itemView.findViewById(R.id.tv_date)
//        private val vTop: View = itemView.findViewById(R.id.v_top)
//        private val vBottom: View = itemView.findViewById(R.id.v_bottom)
//        private val bg: View = itemView.findViewById(R.id.v_bg)
//
//        fun bind(item: Day) {
//            tv.text = item.dayText
//            tv.strokeColor = Color.WHITE
//            tv.strokeWidthPx = 7f // 여기서 더 키우면 숫자 테두리 더 두꺼워짐
//
//            itemView.alpha = if (item.inMonth) 1.0f else 0.35f
//
//            val textColorRes = when (item.dayOfWeek) {
//                1 -> R.color.sunColor
//                7 -> R.color.satColor
//                else -> R.color.textColor
//            }
//            tv.setTextColor(ContextCompat.getColor(itemView.context, textColorRes))
//
//            if (item.inMonth) {
//                val (top, bottom) = colorProvider(item)
//
//                if (top == null && bottom == null) {
//                    vTop.visibility = View.GONE
//                    vBottom.visibility = View.GONE
//                } else {
//                    if (top != null) {
//                        vTop.visibility = View.VISIBLE
//                        vTop.setBackgroundColor(top)
//                    } else {
//                        vTop.visibility = View.INVISIBLE
//                        vTop.setBackgroundColor(Color.TRANSPARENT)
//                    }
//
//                    if (bottom != null) {
//                        vBottom.visibility = View.VISIBLE
//                        vBottom.setBackgroundColor(bottom)
//                    } else {
//                        vBottom.visibility = View.INVISIBLE
//                        vBottom.setBackgroundColor(Color.TRANSPARENT)
//                    }
//                }
//            } else {
//                vTop.visibility = View.GONE
//                vBottom.visibility = View.GONE
//            }
//
//            val key = keyOf(item)
//            when {
//                selectedKey == key -> bg.setBackgroundResource(R.drawable.bg_calendar_home_fragment_border_selected)
//                item.isToday -> bg.setBackgroundResource(R.drawable.bg_calendar_home_fragment_border_today)
//                else -> bg.setBackgroundResource(R.drawable.bg_calendar_border)
//            }
//
//            itemView.setOnClickListener {
//                if (!item.inMonth) return@setOnClickListener
//                if (isBlocked(item)) return@setOnClickListener // ✅ 이미 등록된 날짜면 클릭 무시
//
//                selectedKey = key
//                notifyDataSetChanged()
//                onClick(item)
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_calendar_home_fragment_day, parent, false)
//        return VH(view)
//    }
//
//    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
//    override fun getItemCount(): Int = items.size
//
//    fun submitList(newItems: List<Day>) {
//        items = newItems
//        selectedKey = null
//        notifyDataSetChanged()
//    }
//
//    fun setSelected(day: Day) {
//        selectedKey = keyOf(day)
//        notifyDataSetChanged()
//    }
//
//    private fun keyOf(d: Day): String = "%04d-%02d-%02d".format(d.year, d.month0 + 1, d.dayOfMonth)
// }

package com.ssafy.closetory.homeActivity.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.closetory.R
import com.ssafy.closetory.homeActivity.aiStyling.Day
import com.ssafy.closetory.util.StrokeTextView

private const val TAG = "HomeCalendarAdapter"

class HomeCalendarAdapter(
    private var items: List<Day>,
    private val onClick: (Day) -> Unit,
    private val colorProvider: (Day) -> Pair<Int?, Int?>,
    private val isBlocked: (Day) -> Boolean
) : RecyclerView.Adapter<HomeCalendarAdapter.VH>() {

    private var selectedKey: String? = null

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv: StrokeTextView = itemView.findViewById(R.id.tv_date)
        private val vTop: View = itemView.findViewById(R.id.v_top)
        private val vBottom: View = itemView.findViewById(R.id.v_bottom)
        private val bg: View = itemView.findViewById(R.id.v_bg)

        fun bind(item: Day) {
            tv.text = item.dayText
            tv.strokeColor = Color.WHITE
            tv.strokeWidthPx = 7f

            // 이번 달이 아니면 흐리게
            itemView.alpha = if (item.inMonth) 1.0f else 0.35f

            // 텍스트 색상 (일/토 구분)
            val textColorRes = when (item.dayOfWeek) {
                1 -> R.color.sunColor

                // 일요일
                7 -> R.color.satColor

                // 토요일
                else -> R.color.textColor
            }
            tv.setTextColor(ContextCompat.getColor(itemView.context, textColorRes))

            // ✅ 색상 표시 (이번 달 날짜만)
            if (item.inMonth) {
                val (topColor, bottomColor) = colorProvider(item)

                Log.d(
                    TAG,
                    "🎨 날짜: ${item.year}-${String.format(
                        "%02d",
                        item.month0 + 1
                    )}-${String.format("%02d", item.dayOfMonth)}"
                )
                Log.d(TAG, "   topColor: $topColor, bottomColor: $bottomColor")

                // 상의 색상
                if (topColor != null) {
                    vTop.visibility = View.VISIBLE
                    vTop.setBackgroundColor(topColor)
                } else {
                    vTop.visibility = View.GONE
                }

                // 하의 색상
                if (bottomColor != null) {
                    vBottom.visibility = View.VISIBLE
                    vBottom.setBackgroundColor(bottomColor)
                } else {
                    vBottom.visibility = View.GONE
                }
            } else {
                // 이번 달이 아니면 색상 숨김
                vTop.visibility = View.GONE
                vBottom.visibility = View.GONE
            }

            // 배경 테두리 (선택 > 오늘 > 기본)
            val key = keyOf(item)
            when {
                selectedKey == key -> {
                    bg.setBackgroundResource(R.drawable.bg_calendar_home_fragment_border_selected)
                }

                item.isToday -> {
                    bg.setBackgroundResource(R.drawable.bg_calendar_home_fragment_border_today)
                }

                else -> {
                    bg.setBackgroundResource(R.drawable.bg_calendar_border)
                }
            }

            // 클릭 이벤트
            itemView.setOnClickListener {
                if (!item.inMonth) return@setOnClickListener
                // ✅ 등록된 날짜도 클릭 가능 (isBlocked는 향후 수정/삭제 구현 시 사용)
                // if (isBlocked(item)) return@setOnClickListener

                selectedKey = key
                notifyDataSetChanged()
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_home_fragment_day, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Day>) {
        items = newItems
        selectedKey = null
        notifyDataSetChanged()
    }

    fun setSelected(day: Day) {
        selectedKey = keyOf(day)
        notifyDataSetChanged()
    }

    private fun keyOf(d: Day): String = "%04d-%02d-%02d".format(d.year, d.month0 + 1, d.dayOfMonth)
}
