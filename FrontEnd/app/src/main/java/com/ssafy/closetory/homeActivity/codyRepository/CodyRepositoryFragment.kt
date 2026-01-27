package com.ssafy.closetory.homeActivity.codyRepository

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentCodyRepositoryBinding

class CodyRepositoryFragment :
    BaseFragment<FragmentCodyRepositoryBinding>(
        FragmentCodyRepositoryBinding::bind,
        R.layout.fragment_cody_repository
    ) {

    private val codyAdapter = CodyAdapter() // 어댑터 인스턴스 1개

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 화면 배열을 설정하는 곳
        binding.rvCodyRepository.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = codyAdapter // 같은 인스턴스를 연결
        }

        codyAdapter.submitItems(List(12) { "dummy" }) // 같은 인스턴스에 데이터 주입
    }
}
