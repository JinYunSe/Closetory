package com.ssafy.closetory.util.image

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.NormalizedKeypoint
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenterResult
import com.google.mediapipe.tasks.vision.interactivesegmenter.InteractiveSegmenter
import com.google.mediapipe.tasks.vision.interactivesegmenter.InteractiveSegmenter.InteractiveSegmenterOptions

class MpImageSegmenter(context: Context) {

    private val segmenter: InteractiveSegmenter

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("models/magic_touch.tflite")
            .build()

        val options = InteractiveSegmenterOptions.builder()
            .setBaseOptions(baseOptions)
            .setOutputCategoryMask(true)
            .setOutputConfidenceMasks(false)
            .build()

        segmenter = InteractiveSegmenter.createFromOptions(context, options)
    }

    fun segmentAtPoint(bitmap: Bitmap, xNorm: Float, yNorm: Float): ImageSegmenterResult {
        val mpImage: MPImage = BitmapImageBuilder(bitmap).build()
        val roi = InteractiveSegmenter.RegionOfInterest.create(
            NormalizedKeypoint.create(
                xNorm.coerceIn(0f, 1f),
                yNorm.coerceIn(0f, 1f)
            )
        )
        return segmenter.segment(mpImage, roi)
    }

    fun close() {
        segmenter.close()
    }
}
