package com.ssafy.closetory.homeActivity.mypage

import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.ssafy.closetory.R

class PhotoViewerDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_URL = "ARG_URL"
        private const val ARG_URI = "ARG_URI"

        fun newInstance(url: String): PhotoViewerDialogFragment = PhotoViewerDialogFragment().apply {
            arguments = Bundle().apply { putString(ARG_URL, url) }
        }

        fun newInstance(uri: Uri): PhotoViewerDialogFragment = PhotoViewerDialogFragment().apply {
            arguments = Bundle().apply { putString(ARG_URI, uri.toString()) }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_photo_view, null)
        val imageView = view.findViewById<ImageView>(R.id.photoView)

        val url = arguments?.getString(ARG_URL)
        val uriStr = arguments?.getString(ARG_URI)

        when {
            !url.isNullOrBlank() -> {
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(imageView)
            }

            !uriStr.isNullOrBlank() -> {
                imageView.setImageURI(Uri.parse(uriStr))
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
