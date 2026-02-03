package com.ssafy.closetory.homeActivity.aiStyling
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.closetory.R

class CalendarAdapter(private var items: List<Day>, private val onClick: (Day) -> Unit) :
    RecyclerView.Adapter<CalendarAdapter.VH>() {

    private var selectedKey: String? = null // "yyyy-mm-dd"

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv: TextView = itemView.findViewById(R.id.tv_date)
        private val bg: View = itemView.findViewById(R.id.v_bg)

        fun bind(item: Day) {
            tv.text = item.dayText

            // 기본 투명도(이번달 아니면 흐리게)
            itemView.alpha = if (item.inMonth) 1.0f else 0.35f

            // 텍스트 색: 일요일/토요일
            val color = when (item.dayOfWeek) {
                1 -> R.color.sunColor
                7 -> R.color.satColor
                else -> R.color.textColor
            }
            tv.setTextColor(ContextCompat.getColor(itemView.context, color))

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
                selectedKey = key
                notifyDataSetChanged()
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar, parent, false)
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
