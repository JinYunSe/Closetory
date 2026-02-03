package com.ssafy.closetory.homeActivity.mypage

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.ssafy.closetory.R

class PieMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {

    private val tvMarker: TextView = findViewById(R.id.tvMarker)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val label = (e?.data ?: "").toString()
        tvMarker.text = label
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF = MPPointF(-(width / 2f), -height.toFloat())
}
