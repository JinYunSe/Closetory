package com.ssafy.closetory.homeActivity.codyRepository

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentCodyDetailBinding
import com.ssafy.closetory.util.CalendarPickerDialogFragment

private const val TAG = "CodyDetailFragment"

class CodyDetailFragment :
    BaseFragment<FragmentCodyDetailBinding>(
        FragmentCodyDetailBinding::bind,
        R.layout.fragment_cody_detail
    ) {

    private var lookId: Int = -1
    private var photoUrl: String = ""
    private var date: String = ""
    private var aiReason: String? = null
    private var onlyMine: Boolean = false

    private var selectedCalendarDate: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "CodyDetailFragment onViewCreated")

        // Bundle에서 데이터 받기
        receiveArguments()

        // 캘린더 결과 리스너 설정
        setupCalendarResultListener()

        setupUI()
        setupListeners()
    }

    private fun receiveArguments() {
        arguments?.let { bundle ->
            lookId = bundle.getInt("lookId", -1)
            photoUrl = bundle.getString("photoUrl", "")
            date = bundle.getString("date", "")
            aiReason = bundle.getString("aiReason")
            onlyMine = bundle.getBoolean("onlyMine", false)

            Log.d(TAG, "받은 데이터 - lookId: $lookId, date: $date, aiReason: $aiReason")
        }
    }

    private fun setupCalendarResultListener() {
        // CalendarPickerDialogFragment에서 날짜 선택 결과 받기
        setFragmentResultListener(CalendarPickerDialogFragment.REQ_KEY) { _, bundle ->
            val pickedDate = bundle.getString(CalendarPickerDialogFragment.BUNDLE_KEY_DATE)
            if (pickedDate != null) {
                onCalendarDateSelected(pickedDate)
            }
        }
    }

    private fun setupUI() {
        // 이미지 로딩
        val fullImageUrl = if (photoUrl.startsWith("http")) {
            photoUrl
        } else {
            "${ApplicationClass.API_BASE_URL}$photoUrl"
        }

        Glide.with(this)
            .load(fullImageUrl)
            .centerCrop()
            .placeholder(R.drawable.bg_slot_empty)
            .error(R.drawable.error)
            .into(binding.ivCodyImage)

        // 날짜 표시
        binding.tvDate.text = date

        // AI 추천 이유 표시 (있을 경우에만)
        if (!aiReason.isNullOrBlank()) {
            binding.tvReasonLabel.visibility = View.VISIBLE
            binding.tvReason.visibility = View.VISIBLE
            binding.tvReason.text = aiReason
        } else {
            binding.tvReasonLabel.visibility = View.GONE
            binding.tvReason.visibility = View.GONE
        }

        // 초기 등록 버튼 상태 (비활성화)
        updateRegisterButton(false)
    }

    private fun setupListeners() {
        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 캘린더 버튼 클릭 - 캘린더 다이얼로그 표시
        binding.btnCalendar.setOnClickListener {
            showCalendarDialog()
        }

        // 등록 버튼 클릭
        binding.btnRegister.setOnClickListener {
            if (selectedCalendarDate != null) {
                registerToCalendar()
            }
        }
    }

    private fun showCalendarDialog() {
        Log.d(TAG, "캘린더 다이얼로그 표시")

        // CalendarPickerDialogFragment 표시
        val dialog = CalendarPickerDialogFragment()
        dialog.show(childFragmentManager, "CalendarPicker")
    }

    private fun onCalendarDateSelected(dateStr: String) {
        // dateStr 형식: "yyyy\nMM-dd" 또는 "yyyy-MM-dd"
        Log.d(TAG, "캘린더에서 선택한 날짜: $dateStr")

        // 줄바꿈 제거하고 형식 통일
        selectedCalendarDate = dateStr.replace("\n", "-")

        // 캘린더 아이콘 숨기고 날짜 표시
        binding.ivCalendarIcon.visibility = View.GONE
        binding.tvSelectedDate.text = selectedCalendarDate

        // 등록 버튼 활성화
        updateRegisterButton(true)

        Toast.makeText(requireContext(), "날짜가 선택되었습니다", Toast.LENGTH_SHORT).show()
    }

    private fun updateRegisterButton(isEnabled: Boolean) {
        binding.btnRegister.isEnabled = isEnabled

        if (isEnabled) {
            // 활성화 상태 - 메인 컬러
            binding.cardRegister.setCardBackgroundColor(
                resources.getColor(R.color.main_color, null)
            )
            binding.btnRegister.alpha = 1.0f
        } else {
            // 비활성화 상태 - 회색
            binding.cardRegister.setCardBackgroundColor(
                resources.getColor(android.R.color.darker_gray, null)
            )
            binding.btnRegister.alpha = 0.5f
        }
    }

    private fun registerToCalendar() {
        Log.d(TAG, "캘린더에 등록 - lookId: $lookId, date: $selectedCalendarDate")

        // 예: viewModel.registerLookToCalendar(lookId, selectedCalendarDate)

        Toast.makeText(
            requireContext(),
            "$selectedCalendarDate 에 코디가 등록되었습니다!",
            Toast.LENGTH_LONG
        ).show()

        // 등록 후 뒤로가기
        findNavController().popBackStack()
    }
}
