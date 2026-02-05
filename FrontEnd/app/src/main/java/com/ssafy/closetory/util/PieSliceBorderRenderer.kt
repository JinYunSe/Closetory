package com.ssafy.closetory.util

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.annotation.ColorInt
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet
import com.github.mikephil.charting.renderer.PieChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

class PieSliceBorderRenderer(
    chart: PieChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler,
    @ColorInt borderColor: Int,
    borderWidth: Float
) : PieChartRenderer(chart, animator, viewPortHandler) {

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = borderColor
        strokeWidth = borderWidth
    }

    fun updateBorder(@ColorInt color: Int, width: Float) {
        borderPaint.color = color
        borderPaint.strokeWidth = width
    }

    override fun drawDataSet(c: Canvas, dataSet: IPieDataSet) {
        super.drawDataSet(c, dataSet)

        if (borderPaint.strokeWidth <= 0f || dataSet.entryCount == 0) return

        val chart = mChart
        val center = chart.centerCircleBox
        val radius = chart.radius
        val holeRadius = if (chart.isDrawHoleEnabled) radius * (chart.holeRadius / 100f) else 0f

        val outerRect = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )
        val innerRect = RectF(
            center.x - holeRadius,
            center.y - holeRadius,
            center.x + holeRadius,
            center.y + holeRadius
        )

        val rotationAngle = chart.rotationAngle
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY
        val drawAngles = chart.drawAngles
        val absoluteAngles = chart.absoluteAngles

        val path = Path()
        for (i in 0 until dataSet.entryCount) {
            val startAngle = rotationAngle + (if (i == 0) 0f else absoluteAngles[i - 1]) * phaseX
            val sweepAngle = drawAngles[i] * phaseY
            if (sweepAngle <= 0f) continue

            path.reset()
            path.arcTo(outerRect, startAngle, sweepAngle)
            if (chart.isDrawHoleEnabled && holeRadius > 0f) {
                path.arcTo(innerRect, startAngle + sweepAngle, -sweepAngle)
            } else {
                path.lineTo(center.x, center.y)
            }
            path.close()

            c.drawPath(path, borderPaint)
        }
    }
}
