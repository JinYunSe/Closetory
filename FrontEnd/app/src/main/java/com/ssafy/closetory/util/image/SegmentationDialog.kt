package com.ssafy.closetory.util.image

import android.graphics.Bitmap
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ssafy.closetory.R
import com.ssafy.closetory.homeActivity.registrationCloth.ScribbleOverlayView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object SegmentationDialog {

    fun show(fragment: Fragment, sourceBitmap: Bitmap, onApply: (Bitmap) -> Unit) {
        val view = fragment.layoutInflater.inflate(R.layout.dialog_segment_preview, null)

        val imageView = view.findViewById<ImageView>(R.id.ivPreview)
        val overlay = view.findViewById<ScribbleOverlayView>(R.id.scribbleOverlay)
        val progress = view.findViewById<ProgressBar>(R.id.pb)

        val btnApply = view.findViewById<Button>(R.id.btnApply)
        val btnReset = view.findViewById<Button>(R.id.btnReset)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        imageView.setImageBitmap(sourceBitmap)
        progress.visibility = View.GONE
        btnApply.isEnabled = false

        var resultBitmap: Bitmap? = null

        val dialog = AlertDialog.Builder(fragment.requireContext())
            .setView(view)
            .setCancelable(false)
            .create()

        overlay.setOnStrokeEndListener { points ->
            if (points.size < 3) return@setOnStrokeEndListener

            progress.visibility = View.VISIBLE

            fragment.viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                val cutout = SimpleMaskUtil.cutInsidePolygon(
                    sourceBitmap,
                    imageView,
                    points
                )
                withContext(Dispatchers.Main) {
                    progress.visibility = View.GONE
                    imageView.setImageBitmap(cutout)
                    resultBitmap = cutout
                    btnApply.isEnabled = true
                    overlay.clear()
                }
            }
        }

        btnApply.setOnClickListener {
            resultBitmap?.let(onApply)
            dialog.dismiss()
        }

        btnReset.setOnClickListener {
            imageView.setImageBitmap(sourceBitmap)
            overlay.clear()
            btnApply.isEnabled = false
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}
