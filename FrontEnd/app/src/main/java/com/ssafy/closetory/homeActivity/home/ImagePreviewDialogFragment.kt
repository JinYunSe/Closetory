package com.ssafy.closetory.homeActivity.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.ssafy.closetory.R
import com.ssafy.closetory.databinding.DialogImagePreviewBinding

class ImagePreviewDialogFragment : DialogFragment() {

    private var _binding: DialogImagePreviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val url = requireArguments().getString(ARG_URL).orEmpty()

        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.placeholder) // 프로젝트에 맞게 교체
            .error(R.drawable.error) // 프로젝트에 맞게 교체
            .into(binding.ivPreview)

        binding.btnClose.setOnClickListener { dismissAllowingStateLoss() }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_URL = "arg_url"

        fun newInstance(url: String): ImagePreviewDialogFragment = ImagePreviewDialogFragment().apply {
            arguments = Bundle().apply { putString(ARG_URL, url) }
        }
    }
}
