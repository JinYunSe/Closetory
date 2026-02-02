package com.ssafy.closetory.homeActivity.home

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.ssafy.closetory.R

class ImagePreviewActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_IMAGE_URL = "extra_image_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)

        val ivPreview = findViewById<ImageView>(R.id.iv_image_preview)
        ivPreview.setOnClickListener { finish() }

        val url = intent.getStringExtra(EXTRA_IMAGE_URL)
        if (url.isNullOrBlank()) {
            Toast.makeText(this, "이미지가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Glide.with(this)
            .load(url)
            .error(R.drawable.bg_slot_empty)
            .listener(object : RequestListener<android.graphics.drawable.Drawable> {
                override fun onResourceReady(
                    resource: android.graphics.drawable.Drawable,
                    model: Any,
                    target: Target<android.graphics.drawable.Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource,
                    isFirstResource: Boolean
                ): Boolean = false

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    Toast.makeText(this@ImagePreviewActivity, "이미지 로드 실패.", Toast.LENGTH_SHORT).show()
                    return false
                }
            })
            .into(ivPreview)
    }
}
