package com.ssafy.closetory.homeActivity.codyRepository

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentCodyRepositoryBinding
import com.ssafy.closetory.dto.CodyRepositoryResponse
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.util.ui.BalloonTooltip

private const val TAG = "CodyRepositoryFragment"

class CodyRepositoryFragment :
    BaseFragment<FragmentCodyRepositoryBinding>(
        FragmentCodyRepositoryBinding::bind,
        R.layout.fragment_cody_repository
    ) {

    private val viewModel: CodyRepositoryViewModel by viewModels()

    private val codyAdapter: CodyAdapter by lazy {
        CodyAdapter { item -> onClickCody(item) }
    }

    private var helpTooltip: BalloonTooltip? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "CodyRepositoryFragment onViewCreated")

        val homeActivity = requireActivity() as? HomeActivity
        homeActivity?.let { helpTooltip = BalloonTooltip(it) }

        setupRecyclerView()
        setupListeners()
        setupObservers()

        viewModel.getLooks()
    }

    override fun onDestroyView() {
        helpTooltip?.dismiss()
        helpTooltip = null
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        binding.rvCodyRepository.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = codyAdapter
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun setupObservers() {
        viewModel.looks.observe(viewLifecycleOwner) { list ->
            Log.d(TAG, "룩 목록 업데이트 - ${list.size}개")
            codyAdapter.submitItems(list)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d(TAG, "로딩 상태: $isLoading")
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Log.e(TAG, "에러 발생: $it")
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        }
    }

    private fun onClickCody(item: CodyRepositoryResponse) {
        Log.d(
            TAG,
            "코디 클릭 - lookId=${item.lookId}, onlyMine=${item.onlyMine}, aiReasonLen=${item.aiReason?.length ?: 0}"
        )

        val bundle = Bundle().apply {
            putInt("lookId", item.lookId)
            putString("photoUrl", item.photoUrl)
            putString("date", item.date ?: "")
            // ✅ null 방어 (null이면 상세에서 getString 시 null로 떨어질 수 있음)
            putString("aiReason", item.aiReason ?: "")
            putBoolean("onlyMine", item.onlyMine)
        }

        findNavController().navigate(R.id.navigation_cody_detail, bundle)
    }
}
