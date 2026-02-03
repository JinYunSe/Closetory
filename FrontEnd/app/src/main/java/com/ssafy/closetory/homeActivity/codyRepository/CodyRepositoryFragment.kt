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

        // BalloonTooltip 초기화
        val homeActivity = requireActivity() as? HomeActivity
        homeActivity?.let {
            helpTooltip = BalloonTooltip(it)
        }

        setupRecyclerView()
        setupListeners()
        setupObservers()

        // 페이지 진입 시 룩 목록 로드
        viewModel.getLooks()

        // 도움말 버튼 설정
        binding.btnClosetoryGuide.setOnClickListener { v ->
            helpTooltip?.show(
                anchor = v,
                message = """
                    👕 우측 상단에 로고 뱃지가 있는 코디만
                    캘린더에 등록할 수 있어요.
                    코디를 선택하면 상세 화면에서
                    날짜를 선택하고 등록할 수 있습니다.
                """.trimIndent(),
                autoDismissMs = 3500
            )
        }
    }

    override fun onDestroyView() {
        // 툴팁 정리
        helpTooltip?.dismiss()
        helpTooltip = null
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        binding.rvCodyRepository.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = codyAdapter
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupObservers() {
        // 목록 데이터 관찰
        viewModel.looks.observe(viewLifecycleOwner) { list ->
            Log.d(TAG, "룩 목록 업데이트 - ${list.size}개")
            codyAdapter.submitItems(list)
        }

        // 로딩 상태 관찰
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d(TAG, "로딩 상태: $isLoading")
            // 로딩 UI가 있다면 여기서 처리
            // binding.progressBar.isVisible = isLoading
        }

        // 에러 메시지 관찰
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Log.e(TAG, "에러 발생: $it")
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        }
    }

    private fun onClickCody(item: CodyRepositoryResponse) {
        Log.d(TAG, "코디 클릭 - lookId: ${item.lookId}")

        // 상세 페이지로 이동
        val bundle = Bundle().apply {
            putInt("lookId", item.lookId)
            putString("photoUrl", item.photoUrl)
            putString("date", item.date)
            putString("aiReason", item.aiReason)
            putBoolean("onlyMine", item.onlyMine)
        }

        // ✅ 상세 페이지로 네비게이션
        findNavController().navigate(R.id.navigation_cody_detail, bundle)
    }
}
