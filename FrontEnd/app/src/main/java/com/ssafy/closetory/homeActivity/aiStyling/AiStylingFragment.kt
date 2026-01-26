package com.ssafy.closetory.homeActivity.aiStyling

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import androidx.navigation.fragment.findNavController
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentAiStylingBinding
import kotlin.math.roundToInt

class AiStylingFragment :
    BaseFragment<FragmentAiStylingBinding>(
        FragmentAiStylingBinding::bind,
        R.layout.fragment_ai_styling
    ) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 직접 코디 생성 버튼 클릭 시 StylingFragment로 이동
        // HomeActivity에서 이미 R.id.navigation_styling를 StylingFragment와 연결해두었기 때문에 이 코드를 사용합니다.
        binding.btnMakeoutfit.setOnClickListener {
            findNavController().navigate(R.id.navigation_styling)
        }
        val text = SpannableString("AI 룩 생성 ")

        text.setSpan(
            RelativeSizeSpan(1.6f), // ↺만 크게
            text.length - 1,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.btnAiMakeoutfit.text = text

        // --- 기존 마우스 휠 스크롤 로직 유지 ---
        binding.root.isFocusableInTouchMode = true
        binding.root.requestFocus()

        binding.root.setOnGenericMotionListener { _, event ->
            if (event.action == MotionEvent.ACTION_SCROLL &&
                event.isFromSource(InputDevice.SOURCE_CLASS_POINTER)
            ) {
                val scroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                val dy = (-scroll * 80).roundToInt()

                val sv = binding.svAiMessage
                if ((dy > 0 && sv.canScrollVertically(1)) || (dy < 0 && sv.canScrollVertically(-1))) {
                    sv.scrollBy(0, dy)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }

        binding.svAiMessage.setOnTouchListener { v, _ ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        parentFragmentManager.setFragmentResultListener(
            CalendarPickerDialogFragment.REQ_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val date =
                bundle.getString(CalendarPickerDialogFragment.BUNDLE_KEY_DATE) ?: return@setFragmentResultListener
            binding.btnSelectDate.text = date // 예: 2026-01-19
        }

        binding.btnSelectDate.setOnClickListener {
            CalendarPickerDialogFragment().show(parentFragmentManager, "CalendarPickerDialog")
        }

        // 초기 라벨 세팅(스위치 기본값에 맞춰)
        binding.tvStyleMode.text = if (binding.switchStyleMode.isChecked) "추구" else "어울림"

        // 토글될 때 라벨만 변경
        binding.switchStyleMode.setOnCheckedChangeListener { _, isChecked ->
            binding.tvStyleMode.text = if (isChecked) "추구" else "어울림"
        }

        binding.tvSwitchOwnedOnly.text = if (binding.switchSwitchOwnedOnly.isChecked) "내 옷만" else "모든 옷"

        binding.switchSwitchOwnedOnly.setOnCheckedChangeListener { _, isChecked ->
            binding.tvSwitchOwnedOnly.text = if (isChecked) "내 옷만" else "모든 옷"
        }

        // 보유한 옷만 보기 스위치
//        binding.switchSwitchOwnedOnly.setOnCheckedChangeListener { _, isChecked ->
//            updateSwitchText(isChecked)
//            viewModel.loadClothItems(onlyMine = isChecked)
//        }
    }
}
