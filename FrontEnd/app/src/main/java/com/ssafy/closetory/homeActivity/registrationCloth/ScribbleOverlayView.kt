// app/src/main/java/com/ssafy/closetory/homeActivity/registrationCloth/ScribbleOverlayView.kt
package com.ssafy.closetory.homeActivity.registrationCloth

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt

/**
 * 사용자가 화면 위에 "도형(스크리블)"을 그리게 하는 Overlay View
 * - 손을 떼면(points) 콜백으로 넘겨줌
 */
class ScribbleOverlayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {

    private val path = Path()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val points = ArrayList<PointF>()
    private var lastX = -1f
    private var lastY = -1f

    private var onStrokeEnd: ((List<PointF>) -> Unit)? = null

    fun setOnStrokeEndListener(listener: (List<PointF>) -> Unit) {
        onStrokeEnd = listener
    }

    fun clear() {
        path.reset()
        points.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                clear()
                path.moveTo(x, y)
                addPoint(x, y)
                lastX = x
                lastY = y
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                // 너무 촘촘하면 포인트 많아져서 성능 저하 → 일정 거리 이상일 때만 기록
                if (distance(x, y, lastX, lastY) >= 8f) {
                    path.lineTo(x, y)
                    addPoint(x, y)
                    lastX = x
                    lastY = y
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                addPoint(x, y)
                invalidate()
                onStrokeEnd?.invoke(points.toList())
                return true
            }
        }
        return false
    }

    private fun addPoint(x: Float, y: Float) {
        points.add(PointF(x, y))
        // 안전장치: 포인트 무한 증가 방지
        if (points.size > 1024) points.removeAt(0)
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }
}
