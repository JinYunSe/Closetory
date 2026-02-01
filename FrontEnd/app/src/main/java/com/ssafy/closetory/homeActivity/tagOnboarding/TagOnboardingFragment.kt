package com.ssafy.closetory.homeActivity.tagOnboarding

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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

        val options = listOf(
            TagOption(1, "캐주얼"),
            TagOption(2, "스트릿"),
            TagOption(3, "댄디"),
            TagOption(4, "모던"),
            TagOption(5, "빈티지"),
            TagOption(6, "페미닌"),
            TagOption(7, "놈코어"),
            TagOption(8, "미니멀리즘"),
            TagOption(9, "맥시멀리즘"),
            TagOption(10, "아메리칸캐주얼"),
            TagOption(11, "레이어드"),
            TagOption(12, "클래식"),
            TagOption(13, "스포티"),
            TagOption(14, "에스닉"),
            TagOption(15, "아방가르드")
        )

        adapter = TagOnboardingAdapter(options) { selectedIds ->
            selected = selectedIds
            // 예: 최소 3개 이상 선택해야 활성화
            binding.btnFinish.isEnabled = selected.size >= 3
        }

        binding.rvTags.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvTags.adapter = adapter

        binding.btnFinish.isEnabled = false
        binding.btnFinish.setOnClickListener {
            // 선택 개수 방어(버튼 enabled로도 막지만 한번 더)
            if (selected.size < 3) {
                Toast.makeText(requireContext(), "최소 3개 이상 선택해 주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 중복 클릭 방지(선택)
            binding.btnFinish.isEnabled = false

            // POST 호출
            viewModel.postTagOnboarding(userId, selected)
        }

        // 성공 시
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.submitSuccess.collect {
                    Toast.makeText(requireContext(), "선호 태그 선택 완료", Toast.LENGTH_SHORT).show()

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
        // 실패 시
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.submitFailMessage.collect { msg ->
                    binding.btnFinish.isEnabled = true // 실패하면 다시 활성화
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
