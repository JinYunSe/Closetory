package com.ssafy.closetory.util.image

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

object ImageMultipartUtil {

    fun uriToCompressedMultipart(
        context: Context,
        uri: Uri,
        partName: String = "photo",
        maxBytes: Int = 600 * 1024,
        maxDimension: Int = 1280,
        minQuality: Int = 40
    ): MultipartBody.Part {
        val cr = context.contentResolver

        // EXIF 기준 회전 각도를 읽어서 카메라 사진 방향을 정상화
        val rotation = readExifRotation(cr, uri)

        // 실제 비트맵 로드 없이 원본 가로/세로만 읽어와 다운샘플 비율 계산
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        // maxDimension을 넘지 않도록 디코딩 단계에서 inSampleSize로 대략적인 해상도 축소(OOM 방지)
        val sample = calculateInSampleSize(
            srcW = bounds.outWidth,
            srcH = bounds.outHeight,
            reqW = maxDimension,
            reqH = maxDimension
        )

        // 다운샘플 비율을 적용해 실제 비트맵을 로드
        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        var bmp = cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, decodeOpts) }
            ?: throw IllegalStateException("이미지를 디코딩할 수 없습니다: $uri")

        // 최종 해상도를 maxDimension 이하로 맞춰 전송 용량을 크게 줄임
        bmp = scaleDownToMaxDimension(bmp, maxDimension)

        // EXIF 회전이 있으면 실제 픽셀을 회전시켜 서버/클라이언트에서 뒤집힘 방지
        if (rotation != 0) bmp = rotateBitmap(bmp, rotation)

        // JPEG로 재인코딩하면서 품질(quality)을 낮춰 목표 용량(maxBytes) 이내로 맞춤
        var quality = 85
        var jpegBytes = compressJpeg(bmp, quality)

        while (jpegBytes.size > maxBytes && quality > minQuality) {
            quality -= 8
            jpegBytes = compressJpeg(bmp, quality)
        }

        // 최종 JPEG 바이트 배열을 multipart 파일 파트로 포장해서 서버로 전송
        val requestBody = jpegBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val fileName = "upload_${System.currentTimeMillis()}.jpg"

        return MultipartBody.Part.createFormData(partName, fileName, requestBody)
    }

    private fun readExifRotation(cr: ContentResolver, uri: Uri): Int = cr.openInputStream(uri)?.use { input ->
        try {
            val exif = ExifInterface(input)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (_: Exception) {
            0
        }
    } ?: 0

    private fun calculateInSampleSize(srcW: Int, srcH: Int, reqW: Int, reqH: Int): Int {
        if (srcW <= 0 || srcH <= 0) return 1
        var inSampleSize = 1
        val halfW = srcW / 2
        val halfH = srcH / 2
        // 디코딩 시점에서 가로/세로가 reqW/reqH 이상이면 2배씩 줄이는 비율을 키움
        while ((halfW / inSampleSize) >= reqW && (halfH / inSampleSize) >= reqH) {
            inSampleSize *= 2
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun scaleDownToMaxDimension(bmp: Bitmap, maxDim: Int): Bitmap {
        val w = bmp.width
        val h = bmp.height
        val maxSide = maxOf(w, h)
        // 이미 충분히 작으면 그대로 사용
        if (maxSide <= maxDim) return bmp
        // 큰 변을 maxDim으로 맞추고 비율 유지
        val ratio = maxDim.toFloat() / maxSide.toFloat()
        val nw = (w * ratio).roundToInt()
        val nh = (h * ratio).roundToInt()
        return Bitmap.createScaledBitmap(bmp, nw, nh, true)
    }

    private fun rotateBitmap(bmp: Bitmap, degree: Int): Bitmap {
        val m = Matrix().apply { postRotate(degree.toFloat()) }
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }

    private fun compressJpeg(bmp: Bitmap, quality: Int): ByteArray {
        val out = ByteArrayOutputStream()
        // JPEG quality를 조절해 파일 용량을 줄임(quality가 낮을수록 용량 감소)
        bmp.compress(Bitmap.CompressFormat.JPEG, quality, out)
        return out.toByteArray()
    }
}
