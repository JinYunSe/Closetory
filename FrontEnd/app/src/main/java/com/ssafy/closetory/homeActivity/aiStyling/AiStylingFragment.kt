package com.ssafy.closetory.homeActivity.aiStyling

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentAiStylingBinding
import kotlin.math.roundToInt

class AiStylingFragment :
    BaseFragment<FragmentAiStylingBinding>(
        FragmentAiStylingBinding::bind,
        R.layout.fragment_ai_styling
    ) {

    private val viewModel: AiStylingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()
        setupListeners()
        setupVideoLoading()
    }

    private fun setupUI() {
        binding.tvStyleMode.text = if (binding.switchStyleMode.isChecked) "추구" else "어울림"
        binding.tvSwitchOwnedOnly.text = if (binding.switchSwitchOwnedOnly.isChecked) "내 옷만" else "모든 옷"
        binding.tvAiMessage.text = "AI가 여기에 답변을 해줍니다."
        setupMouseWheelScroll()
    }

    private fun setupObservers() {
        // ⭐ 로딩 상태 관찰
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            updateMainButton()
            updateVideoAnimation(isLoading)
        }

        // ⭐ 단계 상태 관찰
        viewModel.stage.observe(viewLifecycleOwner) { stage ->
            updateMainButton()
        }

        // AI 추천 이유
        viewModel.aiReason.observe(viewLifecycleOwner) { reason ->
            reason?.let { binding.tvAiMessage.text = it }
        }

        // AI 코디 추천 결과
        viewModel.aiCoordination.observe(viewLifecycleOwner) { coordination ->
            if (coordination == null) {
                clearSlots()
                return@observe
            }

            Log.d("AiStyling", "AI 추천 수신: ${coordination.clothesIdList.size}개")
            fillSlots(coordination)
        }

        // AI 가상피팅 이미지
        viewModel.aiImageUrl.observe(viewLifecycleOwner) { url ->
            if (url.isNullOrBlank()) return@observe

            binding.layoutAiFitting.visibility = View.VISIBLE
            binding.vvAiFitting.visibility = View.GONE

            Glide.with(requireContext())
                .load(url)
                .into(binding.ivAiFittingResult)
        }

        // 성공 메시지
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccessMessage()
            }
        }

        // 에러 메시지
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }
    }

    private fun setupListeners() {
        //  메인 버튼 (단계별 동작)
        binding.btnRegister.setOnClickListener {
            handleMainButtonClick()
        }

        // 직접 만들기 버튼
        binding.btnMakeoutfit.setOnClickListener {
            findNavController().navigate(R.id.navigation_styling)
        }

        // 초기화 버튼
        binding.btnAiStylingReset.setOnClickListener {
            binding.layoutAiFitting.visibility = View.GONE
            viewModel.resetAll()
            binding.tvAiMessage.text = "AI가 여기에 답변을 해줍니다."
            Toast.makeText(requireContext(), "초기화되었습니다.", Toast.LENGTH_SHORT).show()
        }

        // 가상피팅 팝업 닫기
        binding.btnCloseAiFitting.setOnClickListener {
            binding.layoutAiFitting.visibility = View.GONE
        }

        // 스위치 리스너
        binding.switchStyleMode.setOnCheckedChangeListener { _, isChecked ->
            binding.tvStyleMode.text = if (isChecked) "추구" else "어울림"
        }

        binding.switchSwitchOwnedOnly.setOnCheckedChangeListener { _, isChecked ->
            binding.tvSwitchOwnedOnly.text = if (isChecked) "내 옷만" else "모든 옷"
        }
    }

    /**
     * ⭐ 메인 버튼 클릭 처리 (단계별)
     */
    private fun handleMainButtonClick() {
        val stage = viewModel.stage.value ?: AiStylingStage.RECOMMEND
        val isLoading = viewModel.isLoading.value == true

        // 로딩 중이면 무시
        if (isLoading) {
            return
        }

        when (stage) {
            // 1단계: AI 코디추천 실행
            AiStylingStage.RECOMMEND -> {
                val isPersonalized = binding.switchStyleMode.isChecked
                val onlyMine = binding.switchSwitchOwnedOnly.isChecked
                viewModel.requestAiRecommendation(isPersonalized, onlyMine)
            }

            // 2단계: AI 가상피팅 실행
            AiStylingStage.FITTING_READY -> {
                binding.layoutAiFitting.visibility = View.VISIBLE
                binding.vvAiFitting.visibility = View.VISIBLE
                viewModel.requestAiFitting()
            }

            // 3단계: 등록
            AiStylingStage.FITTING_DONE -> {
                viewModel.saveCurrentLook()
            }
        }
    }

    /**
     * ⭐ 메인 버튼 UI 업데이트
     */
    private fun updateMainButton() {
        val stage = viewModel.stage.value ?: AiStylingStage.RECOMMEND
        val isLoading = viewModel.isLoading.value == true

        when (stage) {
            AiStylingStage.RECOMMEND -> {
                if (isLoading) {
                    // AI 코디추천 로딩 중
                    binding.btnRegister.text = ""
                    binding.btnRegister.isEnabled = false
                } else {
                    // 초기 상태
                    binding.btnRegister.text = "✨AI 코디추천"
                    binding.btnRegister.isEnabled = true
                }
            }

            AiStylingStage.FITTING_READY -> {
                if (isLoading) {
                    // AI 가상피팅 로딩 중
                    binding.btnRegister.text = ""
                    binding.btnRegister.isEnabled = false
                } else {
                    // 추천 완료, 가상피팅 대기
                    binding.btnRegister.text = "✨AI 가상피팅"
                    binding.btnRegister.isEnabled = true
                }
            }

            AiStylingStage.FITTING_DONE -> {
                if (isLoading) {
                    // 등록 중
                    binding.btnRegister.text = ""
                    binding.btnRegister.isEnabled = false
                } else {
                    // 가상피팅 완료, 등록 대기
                    binding.btnRegister.text = "등록"
                    binding.btnRegister.isEnabled = true
                }
            }
        }
    }

    /**
     * ⭐ VideoView 초기화 (슬롯 영역 중앙)
     */
    private fun setupVideoLoading() {
        val videoUri = Uri.parse(
            "android.resource://${requireContext().packageName}/${R.raw.vv_ai_fitting_progress}"
        )

        binding.vvAiLoading.apply {
            setVideoURI(videoUri)

            setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = true
                mediaPlayer.setVolume(0f, 0f) // 음소거
            }

            setOnErrorListener { _, what, extra ->
                Log.e("AiStyling", "Video error: what=$what, extra=$extra")
                false
            }
        }
    }

    /**
     * ⭐ VideoView 애니메이션 제어 (슬롯 영역 중앙)
     */
    private fun updateVideoAnimation(isLoading: Boolean) {
        if (isLoading) {
            // 로딩 시작: 슬롯 중앙에 비디오 표시
            binding.vvAiLoading.visibility = View.VISIBLE
            binding.vvAiLoading.start()
            Log.d("AiStyling", "Video animation started at center")
        } else {
            // 로딩 종료: 비디오 숨김
            binding.vvAiLoading.visibility = View.GONE
            binding.vvAiLoading.stopPlayback()
            Log.d("AiStyling", "Video animation stopped")
        }
    }

    private fun fillSlots(coordination: com.ssafy.closetory.dto.AiCoordinationResponse) {
        val map = coordination.clothesIdList.associateBy { it.clothesType.uppercase() }

        setSlot(binding.ivSlotTop, map["TOP"]?.photoUrl)
        setSlot(binding.ivSlotBottom, map["BOTTOM"]?.photoUrl)
        setSlot(binding.ivSlotShoes, map["SHOES"]?.photoUrl)
        setSlot(binding.ivSlotOuter, map["OUTER"]?.photoUrl)
        setSlot(binding.ivSlotAcc, map["ACCESSORY"]?.photoUrl)
        setSlot(binding.ivSlotBag, map["BAG"]?.photoUrl)
    }

    private fun setSlot(imageView: android.widget.ImageView, url: String?) {
        if (url.isNullOrBlank()) {
            imageView.setImageDrawable(null)
            imageView.setBackgroundResource(R.drawable.bg_slot_empty)
            return
        }

        imageView.background = null
        Glide.with(requireContext())
            .load(url)
            .into(imageView)
    }

    private fun clearSlots() {
        setSlot(binding.ivSlotTop, null)
        setSlot(binding.ivSlotBottom, null)
        setSlot(binding.ivSlotShoes, null)
        setSlot(binding.ivSlotOuter, null)
        setSlot(binding.ivSlotAcc, null)
        setSlot(binding.ivSlotBag, null)
    }

    private fun setupMouseWheelScroll() {
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
    }

    override fun onPause() {
        super.onPause()
        binding.vvAiLoading.stopPlayback()
    }
}
