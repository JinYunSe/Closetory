package com.ssafy.closetory.homeActivity.home

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::bind, R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 게시글 더보기 버튼 클릭 이벤트
        setupClickPostBtn()
    }

    // 게시글 더보기 버튼 클릭 처리
    private fun setupClickPostBtn() {
        binding.btnPosts.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_post_list)
        }
    }
}
