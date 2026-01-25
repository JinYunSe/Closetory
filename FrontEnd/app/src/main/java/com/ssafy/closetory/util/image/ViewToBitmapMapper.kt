package com.ssafy.closetory.util.image

import android.graphics.Matrix
import android.graphics.PointF
import android.widget.ImageView

object ViewToBitmapMapper {

    fun map(imageView: ImageView, x: Float, y: Float): PointF? {
        val drawable = imageView.drawable ?: return null
        val inverse = Matrix()
        if (!imageView.imageMatrix.invert(inverse)) return null

        val pts = floatArrayOf(x, y)
        inverse.mapPoints(pts)

        val dw = drawable.intrinsicWidth.toFloat()
        val dh = drawable.intrinsicHeight.toFloat()

        if (pts[0] !in 0f..dw || pts[1] !in 0f..dh) return null
        return PointF(pts[0], pts[1])
    }
}
