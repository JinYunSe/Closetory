package com.ssafy.closetory.homeActivity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.closetory.R
import com.ssafy.closetory.homeActivity.aiStyling.Day

class HomeCalendarAdapter(
    private var items: List<Day>,
    private val onClick: (Day) -> Unit,
    private val colorProvider: (Day) -> Pair<Int?, Int?>
) : RecyclerView.Adapter<HomeCalendarAdapter.VH>() {

    private var selectedKey: String? = null // "yyyy-MM-dd"

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv: TextView = itemView.findViewById(R.id.tv_date)
        private val vTop: View = itemView.findViewById(R.id.v_top)
        private val vBottom: View = itemView.findViewById(R.id.v_bottom)
        private val bg: View = itemView.findViewById(R.id.v_bg)

        fun bind(item: Day) {
            tv.text = item.dayText

            // 기본 투명도(이번달 아니면 흐리게)
            itemView.alpha = if (item.inMonth) 1.0f else 0.35f

            // 텍스트 색: 일요일/토요일
            val colorRes = when (item.dayOfWeek) {
                1 -> R.color.sunColor
                7 -> R.color.satColor
                else -> R.color.textColor
            }
            tv.setTextColor(ContextCompat.getColor(itemView.context, colorRes))

            // 상/하 색상 반영(이번달만)
            if (item.inMonth) {
                val (top, bottom) = colorProvider(item)

                if (top != null) {
                    vTop.visibility = View.VISIBLE
                    vTop.setBackgroundColor(top)
                } else {
                    vTop.visibility = View.INVISIBLE
                }

                if (bottom != null) {
                    vBottom.visibility = View.VISIBLE
                    vBottom.setBackgroundColor(bottom)
                } else {
                    vBottom.visibility = View.INVISIBLE
                }
            } else {
                vTop.visibility = View.INVISIBLE
                vBottom.visibility = View.INVISIBLE
            }

            // 배경 우선순위: 선택 > 오늘 > 기본
            val key = keyOf(item)
            when {
                selectedKey == key -> {
                    bg.setBackgroundResource(R.drawable.bg_calendar_selected)
                    tv.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                }

                item.isToday -> {
                    bg.setBackgroundResource(R.drawable.bg_calendar_today)
                }

                else -> {
                    bg.setBackgroundResource(R.drawable.bg_calendar_border)
                }
            }

            itemView.setOnClickListener {
                if (!item.inMonth) return@setOnClickListener

                // 같은 날짜 재클릭도 동작시키려면(원하면) 아래 주석 해제
                // val isSame = selectedKey == key

                selectedKey = key
                notifyDataSetChanged()
                onClick(item) // 재클릭 포함 매번 호출
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_home_fragment_day, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Day>) {
        items = newItems
        // 월 바뀌면 선택 초기화(원하면 유지도 가능)
        selectedKey = null
        notifyDataSetChanged()
    }

    fun setSelected(day: Day) {
        selectedKey = keyOf(day)
        notifyDataSetChanged()
    }

    private fun keyOf(d: Day): String {
        val m = d.month0 + 1
        return "%04d-%02d-%02d".format(d.year, m, d.dayOfMonth)
    }
}
