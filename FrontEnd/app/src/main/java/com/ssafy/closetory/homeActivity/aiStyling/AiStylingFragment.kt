package com.ssafy.closetory.homeActivity.aiStyling

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    // Activity 스코프로 ViewModel 공유
    private val viewModel: AiStylingViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()
        setupListeners()
        setupVideoLoading()
        setupBackPressHandler()
    }

    private fun setupUI() {
        binding.tvStyleMode.text = if (binding.switchStyleMode.isChecked) "추구" else "어울림"
        binding.tvSwitchOwnedOnly.text = if (binding.switchSwitchOwnedOnly.isChecked) "내 옷만" else "모든 옷"
        binding.tvAiMessage.text = "AI가 여기에 답변을 해줍니다."
        setupMouseWheelScroll()
    }

    private fun setupObservers() {
        // 로딩 상태 관찰
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            updateMainButton()
            updateVideoAnimation(isLoading)
        }

        // 로딩 타입 관찰 (어떤 작업이 로딩 중인지)
        viewModel.loadingType.observe(viewLifecycleOwner) { loadingType ->
            updateVideoAnimation(viewModel.isLoading.value == true)
        }

        // 단계 상태 관찰
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
        viewModel.aiPhotoUrl.observe(viewLifecycleOwner) { url ->
            if (url.isNullOrBlank()) return@observe

            // 가상피팅 완료 시 팝업 열기
            binding.layoutAiFitting.visibility = View.VISIBLE
            binding.ivAiFittingResult.visibility = View.VISIBLE

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
        binding.btnRegister.setOnClickListener {
            handleMainButtonClick()
        }

        // 직접 만들기 버튼
        binding.btnMakeoutfit.setOnClickListener {
            findNavController().navigate(R.id.navigation_styling)
        }

        // 초기화 버튼
        binding.btnAiStylingReset.setOnClickListener {
            showResetConfirmDialog()
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

    // 뒤로가기 버튼 처리
    private fun setupBackPressHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val stage = viewModel.stage.value ?: AiStylingStage.RECOMMEND

                // 가상피팅 완료 후 이미지가 표시된 상태
                if (stage == AiStylingStage.FITTING_DONE) {
                    viewModel.resetAll()
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    return
                }

                // 로딩 중이면 경고
                if (viewModel.isAnyJobRunning()) {
                    showLoadingWarningDialog()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    // 로딩 중 경고 다이얼로그

    private fun showLoadingWarningDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("작업 진행 중")
            .setMessage("AI 작업이 진행 중입니다.\n나가시면 작업이 취소됩니다.\n\n정말 나가시겠습니까?")
            .setPositiveButton("나가기") { _, _ ->
                viewModel.resetAll()
                findNavController().popBackStack()
            }
            .setNegativeButton("계속 진행", null)
            .show()
    }

    // 초기화 확인 다이얼로그
    private fun showResetConfirmDialog() {
        val stage = viewModel.stage.value ?: AiStylingStage.RECOMMEND
        val isLoading = viewModel.isLoading.value == true

        // 로딩 중이면 경고
        if (isLoading) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("작업 진행 중")
                .setMessage("AI 작업이 진행 중입니다.\n초기화하시면 작업이 취소됩니다.")
                .setPositiveButton("초기화") { _, _ ->
                    binding.layoutAiFitting.visibility = View.GONE
                    viewModel.resetAll()
                    binding.tvAiMessage.text = "AI가 여기에 답변을 해줍니다."
                }
                .setNegativeButton("취소", null)
                .show()
            return
        }

        // 결과가 있으면 확인
        if (stage != AiStylingStage.RECOMMEND) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("초기화 확인")
                .setMessage("현재 작업 내용이 초기화됩니다.\n계속하시겠습니까?")
                .setPositiveButton("초기화") { _, _ ->
                    binding.layoutAiFitting.visibility = View.GONE
                    viewModel.resetAll()
                    binding.tvAiMessage.text = "AI가 여기에 답변을 해줍니다."
                }
                .setNegativeButton("취소", null)
                .show()
        } else {
            // 초기 상태면 바로 초기화
        }
    }

    private fun handleMainButtonClick() {
        val stage = viewModel.stage.value ?: AiStylingStage.RECOMMEND
        val isLoading = viewModel.isLoading.value == true

        if (isLoading) {
            return
        }

        when (stage) {
            AiStylingStage.RECOMMEND -> {
                val isPersonalized = binding.switchStyleMode.isChecked
                val onlyMine = binding.switchSwitchOwnedOnly.isChecked
                viewModel.requestAiRecommendation(isPersonalized, onlyMine)
            }

            AiStylingStage.FITTING_READY -> {
                // 가상피팅만 요청 (팝업은 결과가 나왔을 때 열림)
                viewModel.requestAiFitting()
            }

            AiStylingStage.FITTING_DONE -> {
                viewModel.saveCurrentLook()
            }
        }
    }

    private fun updateMainButton() {
        val stage = viewModel.stage.value ?: AiStylingStage.RECOMMEND
        val isLoading = viewModel.isLoading.value == true

        when (stage) {
            AiStylingStage.RECOMMEND -> {
                if (isLoading) {
                    binding.btnRegister.text = "AI 코디 추천 중"
                    binding.btnRegister.isEnabled = false
                } else {
                    binding.btnRegister.text = "AI 코디추천"
                    binding.btnRegister.isEnabled = true
                }
            }

            AiStylingStage.FITTING_READY -> {
                if (isLoading) {
                    binding.btnRegister.text = "AI 가상피팅 생성 중"
                    binding.btnRegister.isEnabled = false
                } else {
                    binding.btnRegister.text = "AI 가상피팅"
                    binding.btnRegister.isEnabled = true
                }
            }

            AiStylingStage.FITTING_DONE -> {
                if (isLoading) {
                    binding.btnRegister.text = "저장 중"
                    binding.btnRegister.isEnabled = false
                } else {
                    binding.btnRegister.text = "등록"
                    binding.btnRegister.isEnabled = true
                }
            }
        }
    }

    private fun setupVideoLoading() {
        val videoUri = Uri.parse(
            "android.resource://${requireContext().packageName}/${R.raw.vv_ai_fitting_progress}"
        )

        // AI 추천 & 가상피팅용 VideoView 설정
        binding.vvAiLoading.apply {
            setVideoURI(videoUri)

            setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = true
                mediaPlayer.setVolume(0f, 0f)
            }

            setOnErrorListener { _, what, extra ->
                Log.e("AiStyling", "Video error: what=$what, extra=$extra")
                false
            }
        }
    }

    private fun updateVideoAnimation(isLoading: Boolean) {
        val loadingType = viewModel.loadingType.value

        when (loadingType) {
            AiStylingViewModel.LoadingType.RECOMMEND -> {
                // AI 추천 중: 메인 화면의 vvAiLoading 표시
                if (isLoading) {
                    binding.vvAiLoading.visibility = View.VISIBLE
                    binding.vvAiLoading.start()
                } else {
                    binding.vvAiLoading.visibility = View.GONE
                    binding.vvAiLoading.stopPlayback()
                }
            }

            AiStylingViewModel.LoadingType.FITTING -> {
                // AI 가상피팅 중: 메인 화면의 vvAiLoading 표시 (팝업은 결과 나올 때 열림)
                if (isLoading) {
                    binding.vvAiLoading.visibility = View.VISIBLE
                    binding.vvAiLoading.start()
                } else {
                    binding.vvAiLoading.visibility = View.GONE
                    binding.vvAiLoading.stopPlayback()
                }
            }

            AiStylingViewModel.LoadingType.SAVING -> {
                // 저장 중: 특별한 애니메이션 없음 (버튼 텍스트만 변경됨)
            }

            else -> {
                // 로딩이 아닌 경우 모든 비디오 정지
                binding.vvAiLoading.visibility = View.GONE
                binding.vvAiLoading.stopPlayback()
            }
        }
    }

    private fun fillSlots(coordination: com.ssafy.closetory.dto.AiCoordinationResponse) {
        val map = coordination.clothesIdList.associateBy { it.clothesType.uppercase() }

        setSlot(binding.ivSlotTop, map["TOP"]?.photoUrl)
        setSlot(binding.ivSlotBottom, map["BOTTOM"]?.photoUrl)
        setSlot(binding.ivSlotShoes, map["SHOES"]?.photoUrl)
        setSlot(binding.ivSlotOuter, map["OUTER"]?.photoUrl)
        setSlot(binding.ivSlotAcc, map["ACCESSORIES"]?.photoUrl)
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
    }

    override fun onPause() {
        super.onPause()
        binding.vvAiLoading.stopPlayback()

        // 가상피팅 완료 후 다른 곳으로 이동 시 초기화
        val stage = viewModel.stage.value ?: AiStylingStage.RECOMMEND
        if (stage == AiStylingStage.FITTING_DONE) {
            viewModel.resetAll()
        }
    }
}
