package com.ssafy.closetory.homeActivity.post.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.ssafy.closetory.databinding.DialogPhotoViewBinding

class PostPhotoDialogFragment : DialogFragment() {

    private var _binding: DialogPhotoViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogPhotoViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val url = requireArguments().getString(ARG_URL).orEmpty()

        // 터치: 핀치줌/드래그는 PhotoView가 자동 지원
        Glide.with(this)
            .load(url)
            .into(binding.photoView)

        // 배경 아무데나 누르면 닫기 (원하면 photoView 제외한 영역 클릭만 닫기로 바꿀 수도 있음)
        binding.root.setOnClickListener { dismissAllowingStateLoss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_URL = "url"

        fun newInstance(url: String): PostPhotoDialogFragment = PostPhotoDialogFragment().apply {
            arguments = Bundle().apply { putString(ARG_URL, url) }
        }
    }
}
