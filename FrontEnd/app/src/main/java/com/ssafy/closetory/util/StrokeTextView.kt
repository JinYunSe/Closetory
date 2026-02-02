package com.ssafy.closetory.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class StrokeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    var strokeColor: Int = Color.WHITE
    var strokeWidthPx: Float = 5f

    private val strokePaint = Paint()

    init {
        // 외곽선 렌더링 프린징/번짐 방지
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        val txt = text?.toString().orEmpty()
        if (txt.isEmpty()) {
            super.onDraw(canvas)
            return
        }

        val fillColor = currentTextColor

        // stroke
        strokePaint.set(paint)
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = strokeWidthPx
        strokePaint.color = strokeColor
        strokePaint.isAntiAlias = true

        val x = compoundPaddingLeft.toFloat()
        val y = baseline.toFloat()
        canvas.drawText(txt, x, y, strokePaint)

        // fill
        setTextColor(fillColor)
        super.onDraw(canvas)
    }
}
