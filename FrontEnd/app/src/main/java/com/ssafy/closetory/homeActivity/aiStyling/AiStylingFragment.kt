package com.ssafy.closetory.homeActivity.aiStyling

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
        applyStageUi(AiStylingStage.RECOMMEND)
    }

    private fun setupUI() {
        binding.tvStyleMode.text = if (binding.switchStyleMode.isChecked) "추구" else "어울림"
        binding.tvSwitchOwnedOnly.text = if (binding.switchSwitchOwnedOnly.isChecked) "내 옷만" else "모든 옷"
        binding.tvAiMessage.text = "AI가 여기에 답변을 해줍니다."
        setupMouseWheelScroll()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // 로딩 중에는 버튼 잠깐 막기 (텍스트는 stage에서 결정)
            binding.btnAiVirtualfitting.isEnabled = !isLoading
            binding.btnRegister.isEnabled = !isLoading && viewModel.stage.value == AiStylingStage.FITTING_DONE
        }

        viewModel.stage.observe(viewLifecycleOwner) { stage ->
            applyStageUi(stage)
        }

        viewModel.aiReason.observe(viewLifecycleOwner) { reason ->
            reason?.let { binding.tvAiMessage.text = it }
        }

        viewModel.aiCoordination.observe(viewLifecycleOwner) { coordination ->
            if (coordination == null) {
                clearSlots()
                return@observe
            }

            Log.d("AiStyling", "AI 추천 수신: ${coordination.clothIdList.size}개")
            fillSlots(coordination)
        }

        viewModel.aiImageUrl.observe(viewLifecycleOwner) { url ->
            if (url.isNullOrBlank()) return@observe

            binding.layoutAiFitting.visibility = View.VISIBLE
            binding.progressAiFitting.visibility = View.GONE

            Glide.with(requireContext())
                .load(url)
                .into(binding.ivAiFittingResult)
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccessMessage()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }
    }

    private fun setupListeners() {
        // AI 버튼: 단계에 따라 동작이 바뀜
        binding.btnAiVirtualfitting.setOnClickListener {
            when (viewModel.stage.value ?: AiStylingStage.RECOMMEND) {
                AiStylingStage.RECOMMEND -> requestAiRecommendation()

                AiStylingStage.FITTING_READY -> {
                    binding.layoutAiFitting.visibility = View.VISIBLE
                    binding.progressAiFitting.visibility = View.VISIBLE
                    viewModel.requestAiFitting()
                }

                AiStylingStage.FITTING_DONE -> {
                    // 이미 가상피팅 끝났으면 굳이 다시 생성 X (원하면 여기서 재생성 로직 가능)
                    Toast.makeText(requireContext(), "이미 가상피팅이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnMakeoutfit.setOnClickListener {
            findNavController().navigate(R.id.navigation_styling)
        }

        // 등록: 가상피팅 완료 전에는 비활성 (applyStageUi에서 제어)
        binding.btnRegister.setOnClickListener {
            if (viewModel.stage.value != AiStylingStage.FITTING_DONE) {
                Toast.makeText(requireContext(), "가상피팅 완료 후 등록할 수 있습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 서버에 룩 저장
            viewModel.saveCurrentLook()
        }

        binding.btnAiStylingReset.setOnClickListener {
            binding.layoutAiFitting.visibility = View.GONE
            viewModel.resetAll()
            binding.tvAiMessage.text = "AI가 여기에 답변을 해줍니다."
            Toast.makeText(requireContext(), "초기화되었습니다.", Toast.LENGTH_SHORT).show()
        }

        binding.btnCloseAiFitting.setOnClickListener {
            binding.layoutAiFitting.visibility = View.GONE
        }

        binding.switchStyleMode.setOnCheckedChangeListener { _, isChecked ->
            binding.tvStyleMode.text = if (isChecked) "추구" else "어울림"
        }

        binding.switchSwitchOwnedOnly.setOnCheckedChangeListener { _, isChecked ->
            binding.tvSwitchOwnedOnly.text = if (isChecked) "내 옷만" else "모든 옷"
        }
    }

    private fun applyStageUi(stage: AiStylingStage) {
        val loading = viewModel.isLoading.value == true

        when (stage) {
            AiStylingStage.RECOMMEND -> {
                binding.btnAiVirtualfitting.text = if (loading) "생성 중..." else "✨AI\n코디생성"
                binding.btnRegister.isEnabled = false
                binding.btnRegister.alpha = 0.4f
                binding.layoutAiFitting.visibility = View.GONE
            }

            AiStylingStage.FITTING_READY -> {
                binding.btnAiVirtualfitting.text = if (loading) "생성 중..." else "✨AI\n가상생성"
                binding.btnRegister.isEnabled = false
                binding.btnRegister.alpha = 0.4f
            }

            AiStylingStage.FITTING_DONE -> {
                binding.btnAiVirtualfitting.text = "✨AI\n가상생성"
                binding.btnRegister.isEnabled = !loading
                binding.btnRegister.alpha = if (loading) 0.4f else 1.0f
            }
        }
    }

    private fun requestAiRecommendation() {
        val isPersonalized = binding.switchStyleMode.isChecked
        val onlyMine = binding.switchSwitchOwnedOnly.isChecked
        viewModel.requestAiRecommendation(isPersonalized, onlyMine)
    }

    private fun fillSlots(coordination: com.ssafy.closetory.dto.AiCoordinationResponse) {
        // clothesType 기반으로 슬롯 매핑
        val map = coordination.clothIdList.associateBy { it.clothesType.uppercase() }

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
}
