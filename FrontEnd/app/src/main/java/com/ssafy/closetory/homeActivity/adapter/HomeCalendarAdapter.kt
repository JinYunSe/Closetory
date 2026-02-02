package com.ssafy.closetory.homeActivity.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.closetory.R
import com.ssafy.closetory.homeActivity.aiStyling.Day
import com.ssafy.closetory.util.StrokeTextView

class HomeCalendarAdapter(
    private var items: List<Day>,
    private val onClick: (Day) -> Unit,
    private val colorProvider: (Day) -> Pair<Int?, Int?>
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
            tv.strokeWidthPx = 7f // 여기서 더 키우면 숫자 테두리 더 두꺼워짐

            itemView.alpha = if (item.inMonth) 1.0f else 0.35f

            val textColorRes = when (item.dayOfWeek) {
                1 -> R.color.sunColor
                7 -> R.color.satColor
                else -> R.color.textColor
            }
            tv.setTextColor(ContextCompat.getColor(itemView.context, textColorRes))

            if (item.inMonth) {
                val (top, bottom) = colorProvider(item)

                if (top == null && bottom == null) {
                    vTop.visibility = View.GONE
                    vBottom.visibility = View.GONE
                } else {
                    if (top != null) {
                        vTop.visibility = View.VISIBLE
                        vTop.setBackgroundColor(top)
                    } else {
                        vTop.visibility = View.INVISIBLE
                        vTop.setBackgroundColor(Color.TRANSPARENT)
                    }

                    if (bottom != null) {
                        vBottom.visibility = View.VISIBLE
                        vBottom.setBackgroundColor(bottom)
                    } else {
                        vBottom.visibility = View.INVISIBLE
                        vBottom.setBackgroundColor(Color.TRANSPARENT)
                    }
                }
            } else {
                vTop.visibility = View.GONE
                vBottom.visibility = View.GONE
            }

            val key = keyOf(item)
            when {
                selectedKey == key -> bg.setBackgroundResource(R.drawable.bg_calendar_home_fragment_border_selected)
                item.isToday -> bg.setBackgroundResource(R.drawable.bg_calendar_home_fragment_border_today)
                else -> bg.setBackgroundResource(R.drawable.bg_calendar_border)
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
