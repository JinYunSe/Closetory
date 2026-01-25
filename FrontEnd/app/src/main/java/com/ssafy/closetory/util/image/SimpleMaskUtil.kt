package com.ssafy.closetory.util.image

import android.graphics.*
import android.widget.ImageView

object SimpleMaskUtil {

    fun cutInsidePolygon(bitmap: Bitmap, imageView: ImageView, viewPoints: List<PointF>): Bitmap {
        val bitmapPoints = viewPoints.mapNotNull {
            ViewToBitmapMapper.map(imageView, it.x, it.y)
        }
        if (bitmapPoints.size < 3) return bitmap

        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)

        val path = Path().apply {
            moveTo(bitmapPoints[0].x, bitmapPoints[0].y)
            for (i in 1 until bitmapPoints.size) {
                lineTo(bitmapPoints[i].x, bitmapPoints[i].y)
            }
            close()
        }

        canvas.save()
        canvas.clipPath(path)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.restore()

        return output
    }
}
