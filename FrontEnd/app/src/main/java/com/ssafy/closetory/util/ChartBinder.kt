package com.ssafy.closetory.util

import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter

fun bindTopRatioToHorizontalBarChart(
    chart: HorizontalBarChart,
    items: List<StatRatioItem>,
    noDataText: String = "데이터가 없습니다."
) {
    if (items.isEmpty()) {
        chart.clear()
        chart.setNoDataText(noDataText)
        chart.invalidate()
        return
    }

    val entries = items.mapIndexed { index, item ->
        BarEntry(index.toFloat(), item.ratio)
    }

    val dataSet = BarDataSet(entries, "")
    dataSet.setDrawValues(true)
    dataSet.valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            // 소수점 1자리 퍼센트
            return String.format("%.1f%%", value)
        }
    }

    val data = BarData(dataSet)
    data.barWidth = 0.55f

    chart.data = data

    // 공통 옵션(필수만)
    chart.description.isEnabled = false
    chart.legend.isEnabled = false
    chart.setDrawValueAboveBar(true)

    // 우측 축 제거
    chart.axisRight.isEnabled = false

    // 좌측 축: 0~100
    chart.axisLeft.axisMinimum = 0f
    chart.axisLeft.axisMaximum = 100f
    chart.axisLeft.granularity = 10f

    // X축: 라벨
    chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
    chart.xAxis.granularity = 1f
    chart.xAxis.setDrawGridLines(false)
    chart.xAxis.valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val idx = value.toInt()
            return items.getOrNull(idx)?.label ?: ""
        }
    }

    // 여백/터치(원하면 조정)
    chart.setFitBars(true)
    chart.setScaleEnabled(false)
    chart.isDoubleTapToZoomEnabled = false

    chart.invalidate()
}
