package com.ssafy.closetory.homeActivity.codyRepository

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentCodyRepositoryBinding
import com.ssafy.closetory.dto.CodyRepositoryResponse

class CodyRepositoryFragment :
    BaseFragment<FragmentCodyRepositoryBinding>(
        FragmentCodyRepositoryBinding::bind,
        R.layout.fragment_cody_repository
    ) {

    private val viewModel: CodyRepositoryViewModel by viewModels()

    private val codyAdapter: CodyAdapter by lazy {
        // 네 Adapter가 클릭 콜백 받는 형태라면 그대로 사용
        CodyAdapter { item -> onClickCody(item) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        setupObservers()

        // 페이지 들어가자 마자 최초로 로드해서 모든 룩을 보여주는 것
        viewModel.loadLooks() // (네 ViewModel 함수명이 다르면 여기만 수정)
    }

    // 페이지에 이미지를 보여주는 형식은 3열 그리드방식
    private fun setupRecyclerView() {
        binding.rvCodyRepository.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = codyAdapter
        }
    }

    private fun setupListeners() {
        // 뒤로가기 버튼으로 마이페이지로 이동
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupObservers() {
        // ✅ 목록 데이터
        viewModel.looks.observe(viewLifecycleOwner) { list ->
            // 네 Adapter가 submitItems를 갖고 있다면 이대로
            codyAdapter.submitItems(list)
        }

        // ✅ 에러 메시지
        viewModel.error.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                // viewModel.clearErrorMessage() 같은 게 있다면 호출
            }
        }

        // (선택) 로딩
        // viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
        //     binding.progressBar.isVisible = loading
        // }
    }

    private fun onClickCody(item: CodyRepositoryResponse) {
        // 상세 페이지로 이동할 거면 Bundle로 전달
        val bundle = Bundle().apply {
            putInt("lookId", item.lookId)
            putString("photoUrl", item.photoUrl)
            putString("date", item.date)
            putString("reason", item.reason) // null 가능
            putBoolean("onlyMine", item.onlyMine)
        }

        // navigation id는 네 mobile_navigation.xml에 맞춰 수정
        // 예: R.id.navigation_cody_detail
//        findNavController().navigate(R.id.navigation_cody_detail, bundle)
    }
}
