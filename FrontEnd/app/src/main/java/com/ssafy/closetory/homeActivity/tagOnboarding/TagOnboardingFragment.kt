package com.ssafy.closetory.homeActivity.tagOnboarding

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.GridLayoutManager
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.databinding.FragmentTagOnboardingBinding
import com.ssafy.closetory.homeActivity.adapter.TagOnboardingAdapter
import kotlinx.coroutines.launch

private const val TAG = "TagOnboardingFragment_싸피"

class TagOnboardingFragment : Fragment(R.layout.fragment_tag_onboarding) {

    private var _binding: FragmentTagOnboardingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TagOnboardingViewModel by viewModels()

    private lateinit var adapter: TagOnboardingAdapter
    private var selected: List<Int> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTagOnboardingBinding.bind(view)

        val userId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID) ?: return

        // (안전장치) userId가 없으면 앱 종료 (혹은 Auth로 보내도 됨)
        if (userId == -1) {
            requireActivity().finish()
            return
        }

        // 온보딩 완료 전에는 뒤로가기 = 앱 종료
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish() // 앱 종료(현재 task 종료)
                }
            }
        )

        binding.rvTags.layoutManager = GridLayoutManager(requireContext(), 3)

        binding.btnFinish.isEnabled = false
        binding.btnFinish.setOnClickListener {
            // 선택 개수 방어(버튼 enabled로도 막지만 한번 더)
            if (selected.size < 3) {
                Toast.makeText(requireContext(), "최소 3개 이상 선택해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 중복 클릭 방지(선택)
            binding.btnFinish.isEnabled = false

            // POST 호출
            viewModel.postTagOnboarding(userId, selected)
        }

        // 서버에서 태그 목록 로드
        viewModel.loadTags()

        // 태그 목록 수신
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tagOptions.collect { options ->
                    if (options.isEmpty()) return@collect

                    // 태그 목록이 다시 들어오면 선택 상태 초기화
                    selected = emptyList()
                    binding.btnFinish.isEnabled = false

                    adapter = TagOnboardingAdapter(options) { selectedIds ->
                        selected = selectedIds
                        binding.btnFinish.isEnabled = selected.size >= 3

                        val color = if (selected.size >= 3) R.color.main_color else R.color.gray_500
                        binding.btnFinish.backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
                    }
                    binding.rvTags.adapter = adapter
                }
            }
        }

        // 태그 목록 로드 실패
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tagLoadFailMessage.collect { msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 성공 시 : 온보딩 완료 처리 + 홈 이동
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.submitSuccess.collect {

                    // 온보딩 완료 저장
                    ApplicationClass.sharedPreferences.setOnboardingDone(userId, true)

                    // 온보딩 완료 후 홈으로 이동
                    findNavController().navigate(
                        R.id.navigation_home,
                        null,
                        navOptions {
                            popUpTo(R.id.navigation_tag_onboarding) { inclusive = true }
                            launchSingleTop = true
                        }
                    )
                }
            }
        }

        // 실패 시 : 버튼 다시 활성화(선택 조건 유지)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.submitFailMessage.collect { msg ->
                    binding.btnFinish.isEnabled = selected.size >= 3
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

