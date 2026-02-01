package com.ssafy.closetory.homeActivity.styling

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentStylingBinding
import com.ssafy.closetory.dto.ClothesItemDto
import com.ssafy.closetory.homeActivity.adapter.ClothesAdapter

private const val TAG = "StylingFragment"

class StylingFragment :
    BaseFragment<FragmentStylingBinding>(
        FragmentStylingBinding::bind,
        R.layout.fragment_styling
    ) {

    // ⭐ Activity 스코프로 ViewModel 공유 (비동기 처리)
    private val viewModel: StylingViewModel by activityViewModels()

    private lateinit var topAdapter: ClothesAdapter
    private lateinit var bottomAdapter: ClothesAdapter
    private lateinit var outerAdapter: ClothesAdapter
    private lateinit var accAdapter: ClothesAdapter
    private lateinit var bagAdapter: ClothesAdapter
    private lateinit var shoeAdapter: ClothesAdapter

    // 순서: Top, Bottom, Shoes, Outer, Accessory, Bag
    private val selectedSlots = mutableMapOf<String, ClothesItemDto?>(
        "TOP" to null,
        "BOTTOM" to null,
        "SHOES" to null,
        "OUTER" to null,
        "ACC" to null,
        "BAG" to null
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "🚀 StylingFragment onViewCreated 시작")

        setupRecyclerViews()
        setupButtons()
        observeViewModel()
        setupVideoLoading()
        setupBackPressHandler()

        // 초기 데이터 로드
        viewModel.loadClothItems(onlyMine = false)
    }

    private fun setupRecyclerViews() {
        topAdapter = ClothesAdapter().apply {
            onItemClick = { item ->
                Log.d(TAG, "상의 클릭: clothesId=${item.clothesId}")
                addItemToSlot("TOP", item, binding.ivSlotTop, binding.btnRemoveTop)
                updateStageAfterSelection()
            }
        }
        binding.lvTopCloth.adapter = topAdapter

        bottomAdapter = ClothesAdapter().apply {
            onItemClick = { item ->
                addItemToSlot("BOTTOM", item, binding.ivSlotBottom, binding.btnRemoveBottom)
                updateStageAfterSelection()
            }
        }
        binding.lvBottomCloth.adapter = bottomAdapter

        outerAdapter = ClothesAdapter().apply {
            onItemClick = { item ->
                addItemToSlot("OUTER", item, binding.ivSlotOuter, binding.btnRemoveOuter)
                updateStageAfterSelection()
            }
        }
        binding.lvOuter.adapter = outerAdapter

        accAdapter = ClothesAdapter().apply {
            onItemClick = { item ->
                addItemToSlot("ACC", item, binding.ivSlotAcc, binding.btnRemoveAcc)
                updateStageAfterSelection()
            }
        }
        binding.lvAccCloth.adapter = accAdapter

        bagAdapter = ClothesAdapter().apply {
            onItemClick = { item ->
                addItemToSlot("BAG", item, binding.ivSlotBag, binding.btnRemoveBag)
                updateStageAfterSelection()
            }
        }
        binding.lvBagCloth.adapter = bagAdapter

        shoeAdapter = ClothesAdapter().apply {
            onItemClick = { item ->
                addItemToSlot("SHOES", item, binding.ivSlotShoes, binding.btnRemoveShoes)
                updateStageAfterSelection()
            }
        }
        binding.lvShoesCloth.adapter = shoeAdapter
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // ⭐ 메인 버튼 (AI 가상피팅 → 등록)
        binding.btnStylingRegister.setOnClickListener {
            handleMainButtonClick()
        }

        binding.btnCloseAiFitting.setOnClickListener {
            hideAiFittingResult()
        }

        binding.switchSwitchOwnedOnly.setOnCheckedChangeListener { _, isChecked ->
            updateSwitchText(isChecked)
            viewModel.loadClothItems(onlyMine = isChecked)
        }

        // 초기화 버튼
        binding.btnStylingReset.setOnClickListener {
            showResetConfirmDialog()
        }

        setupRemoveButtons()
    }

    /**
     * ⭐ 뒤로가기 버튼 처리 (로딩 중이면 경고)
     */
    private fun setupBackPressHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
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

    /**
     * ⭐ 로딩 중 경고 다이얼로그
     */
    private fun showLoadingWarningDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("작업 진행 중")
            .setMessage("AI 가상피팅이 진행 중입니다.\n나가시면 작업이 취소됩니다.\n\n정말 나가시겠습니까?")
            .setPositiveButton("나가기") { _, _ ->
                viewModel.resetAll()
                findNavController().popBackStack()
            }
            .setNegativeButton("계속 진행", null)
            .show()
    }

    /**
     * ⭐ 초기화 확인 다이얼로그
     */
    private fun showResetConfirmDialog() {
        val stage = viewModel.stage.value ?: StylingStage.SELECTING
        val isLoading = viewModel.isLoading.value == true

        if (isLoading) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("작업 진행 중")
                .setMessage("AI 가상피팅이 진행 중입니다.\n초기화하시면 작업이 취소됩니다.")
                .setPositiveButton("초기화") { _, _ ->
                    clearAllSlots()
                }
                .setNegativeButton("취소", null)
                .show()
            return
        }

        if (stage != StylingStage.SELECTING) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("초기화 확인")
                .setMessage("현재 작업 내용이 초기화됩니다.\n계속하시겠습니까?")
                .setPositiveButton("초기화") { _, _ ->
                    clearAllSlots()
                }
                .setNegativeButton("취소", null)
                .show()
        } else {
            clearAllSlots()
        }
    }

    /**
     * ⭐ 메인 버튼 클릭 처리 (단계별)
     */
    private fun handleMainButtonClick() {
        val stage = viewModel.stage.value ?: StylingStage.SELECTING
        val isLoading = viewModel.isLoading.value == true

        if (isLoading) return

        when (stage) {
            StylingStage.SELECTING -> {
                // ⭐ 옷이 선택되지 않은 상태 - 토스트만 표시
                Toast.makeText(requireContext(), "최소 1개 이상의 의류를 선택해주세요", Toast.LENGTH_SHORT).show()
            }

            StylingStage.FITTING_READY -> {
                // ⭐ 가상피팅 요청
                requestAiFitting()
            }

            StylingStage.FITTING_DONE -> {
                // ⭐ 룩 저장
                saveLook()
            }
        }
    }

    /**
     * ⭐ VideoView 초기화 (AI 페이지와 동일)
     */
    private fun setupVideoLoading() {
        val videoUri = Uri.parse(
            "android.resource://${requireContext().packageName}/${R.raw.vv_ai_fitting_progress}"
        )

        binding.vvAiLoading.apply {
            setVideoURI(videoUri)

            setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = true
                Log.d(TAG, "비디오 준비 완료")
            }

            setOnErrorListener { _, what, extra ->
                Log.e(TAG, "비디오 재생 오류: what=$what, extra=$extra")
                true
            }
        }

        Log.d(TAG, "비디오 로딩 초기화 완료")
    }

    /**
     * ⭐ 비디오 애니메이션 제어 (AiStyling과 동일하게 추가)
     */
    private fun updateVideoAnimation(isLoading: Boolean) {
        val loadingType = viewModel.loadingType.value

        Log.d(TAG, "updateVideoAnimation: isLoading=$isLoading, loadingType=$loadingType")

        if (isLoading && loadingType == StylingViewModel.LoadingType.FITTING) {
            // ✅ 가상피팅 로딩 시작
            binding.layoutAiFitting.visibility = View.VISIBLE
            binding.vvAiLoading.visibility = View.VISIBLE
            binding.ivAiFittingResult.visibility = View.GONE

            if (!binding.vvAiLoading.isPlaying) {
                binding.vvAiLoading.start()
                Log.d(TAG, "✅ 비디오 재생 시작")
            }
        } else {
            // ✅ 로딩 종료 - 비디오 숨김
            binding.vvAiLoading.stopPlayback()
            binding.vvAiLoading.visibility = View.GONE
            Log.d(TAG, "❌ 비디오 재생 중지")
        }
    }

    private fun updateSwitchText(isChecked: Boolean) {
        binding.switchSwitchOwnedOnly.text = if (isChecked) "내 옷만 보기" else "전체 옷 보기"
    }

    private fun setupRemoveButtons() {
        binding.btnRemoveTop.setOnClickListener {
            removeItemFromSlot("TOP", binding.ivSlotTop, binding.btnRemoveTop)
        }

        binding.btnRemoveBottom.setOnClickListener {
            removeItemFromSlot("BOTTOM", binding.ivSlotBottom, binding.btnRemoveBottom)
        }

        binding.btnRemoveOuter.setOnClickListener {
            removeItemFromSlot("OUTER", binding.ivSlotOuter, binding.btnRemoveOuter)
        }

        binding.btnRemoveAcc.setOnClickListener {
            removeItemFromSlot("ACC", binding.ivSlotAcc, binding.btnRemoveAcc)
        }

        binding.btnRemoveBag.setOnClickListener {
            removeItemFromSlot("BAG", binding.ivSlotBag, binding.btnRemoveBag)
        }

        binding.btnRemoveShoes.setOnClickListener {
            removeItemFromSlot("SHOES", binding.ivSlotShoes, binding.btnRemoveShoes)
        }
    }

    private fun observeViewModel() {
        // 의류 데이터 관찰
        viewModel.closetData.observe(viewLifecycleOwner) { data ->
            if (data == null) {
                Log.e(TAG, "데이터가 NULL입니다!")
                return@observe
            }

            Log.d(TAG, "의류 데이터 수신")
            topAdapter.submitList(data.topClothes ?: emptyList())
            bottomAdapter.submitList(data.bottomClothes ?: emptyList())
            outerAdapter.submitList(data.outerClothes ?: emptyList())
            accAdapter.submitList(data.accessories ?: emptyList())
            bagAdapter.submitList(data.bags ?: emptyList())
            shoeAdapter.submitList(data.shoes ?: emptyList())
        }

        // ⭐ 로딩 상태 관찰 (비디오 애니메이션 제어)
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            updateMainButton()
            updateVideoAnimation(isLoading)
        }

        // ⭐ 단계 상태 관찰 (버튼 텍스트 변경)
        viewModel.stage.observe(viewLifecycleOwner) { stage ->
            updateMainButton()
        }

        // ⭐ AI 가상 피팅 결과 관찰
        viewModel.aiImageUrl.observe(viewLifecycleOwner) { imageUrl ->
            if (imageUrl != null) {
                showAiFittingResult(imageUrl)
            }
        }

        // 에러 메시지 관찰
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        }

        // 성공 메시지 관찰
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearSuccessMessage()
            }
        }
    }

    /**
     * ⭐ 메인 버튼 UI 업데이트
     */
    private fun updateMainButton() {
        val stage = viewModel.stage.value ?: StylingStage.SELECTING
        val isLoading = viewModel.isLoading.value == true

        when (stage) {
            StylingStage.SELECTING -> {
                // ⭐ 옷 선택 전 - "AI 가상피팅" 텍스트지만 비활성화
                binding.btnStylingRegister.text = "✨AI 가상피팅"
                binding.btnStylingRegister.isEnabled = false
                binding.btnStylingRegister.alpha = 0.5f
            }

            StylingStage.FITTING_READY -> {
                if (isLoading) {
                    // ⭐ 가상피팅 중
                    binding.btnStylingRegister.text = "가상피팅중"
                    binding.btnStylingRegister.isEnabled = false
                    binding.btnStylingRegister.alpha = 0.5f
                } else {
                    // ⭐ 가상피팅 대기 (옷 선택 완료)
                    binding.btnStylingRegister.text = "✨AI 가상피팅"
                    binding.btnStylingRegister.isEnabled = true
                    binding.btnStylingRegister.alpha = 1.0f
                }
            }

            StylingStage.FITTING_DONE -> {
                if (isLoading) {
                    // ⭐ 등록 중
                    binding.btnStylingRegister.text = "등록중"
                    binding.btnStylingRegister.isEnabled = false
                    binding.btnStylingRegister.alpha = 0.5f
                } else {
                    // ⭐ 등록 가능 (가상피팅 완료)
                    binding.btnStylingRegister.text = "등록"
                    binding.btnStylingRegister.isEnabled = true
                    binding.btnStylingRegister.alpha = 1.0f
                }
            }
        }
    }

    /**
     * ⭐ 옷 선택 후 단계 업데이트
     */
    private fun updateStageAfterSelection() {
        val hasSelection = selectedSlots.values.any { it != null }
        viewModel.updateStageAfterSelection(hasSelection)
    }

    private fun addItemToSlot(slotType: String, item: ClothesItemDto, imageView: ImageView, removeButton: View) {
        Log.d(TAG, "addItemToSlot: $slotType, clothesId=${item.clothesId}")

        selectedSlots[slotType] = item

        val imageUrl = if (item.photoUrl.startsWith("http")) {
            item.photoUrl
        } else {
            "${ApplicationClass.API_BASE_URL}${item.photoUrl}"
        }

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.bg_slot_empty)
            .error(R.drawable.bg_slot_empty)
            .centerInside()
            .into(imageView)

        imageView.background = null
        removeButton.visibility = View.VISIBLE
    }

    private fun removeItemFromSlot(slotType: String, imageView: ImageView, removeButton: View) {
        Log.d(TAG, "removeItemFromSlot: $slotType")

        selectedSlots[slotType] = null

        Glide.with(this).clear(imageView)
        imageView.setImageDrawable(null)
        imageView.setBackgroundResource(R.drawable.bg_slot_empty)

        removeButton.visibility = View.GONE

        updateStageAfterSelection()
    }

    private fun clearAllSlots() {
        Log.d(TAG, "clearAllSlots 호출")

        removeItemFromSlot("TOP", binding.ivSlotTop, binding.btnRemoveTop)
        removeItemFromSlot("BOTTOM", binding.ivSlotBottom, binding.btnRemoveBottom)
        removeItemFromSlot("OUTER", binding.ivSlotOuter, binding.btnRemoveOuter)
        removeItemFromSlot("ACC", binding.ivSlotAcc, binding.btnRemoveAcc)
        removeItemFromSlot("BAG", binding.ivSlotBag, binding.btnRemoveBag)
        removeItemFromSlot("SHOES", binding.ivSlotShoes, binding.btnRemoveShoes)

        hideAiFittingResult()
        viewModel.resetAll()

        Toast.makeText(requireContext(), "코디가 초기화되었습니다", Toast.LENGTH_SHORT).show()
    }

    /**
     * ⭐ 룩 저장
     */
    private fun saveLook() {
        Log.d(TAG, "saveLook 호출")

        val clothesIdList = listOf(
            selectedSlots["TOP"]?.clothesId ?: -1,
            selectedSlots["BOTTOM"]?.clothesId ?: -1,
            selectedSlots["SHOES"]?.clothesId ?: -1,
            selectedSlots["OUTER"]?.clothesId ?: -1,
            selectedSlots["ACC"]?.clothesId ?: -1,
            selectedSlots["BAG"]?.clothesId ?: -1
        )

        Log.d(TAG, "전송할 clothesIdList: $clothesIdList")

        if (clothesIdList.all { it == -1 }) {
            Toast.makeText(requireContext(), "최소 1개 이상의 의류를 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveLook(clothesIdList)
    }

    /**
     * ⭐ AI 가상피팅 요청
     */
    private fun requestAiFitting() {
        Log.d(TAG, "requestAiFitting 호출")

        val clothesIdList = listOf(
            selectedSlots["TOP"]?.clothesId ?: -1,
            selectedSlots["BOTTOM"]?.clothesId ?: -1,
            selectedSlots["SHOES"]?.clothesId ?: -1,
            selectedSlots["OUTER"]?.clothesId ?: -1,
            selectedSlots["ACC"]?.clothesId ?: -1,
            selectedSlots["BAG"]?.clothesId ?: -1
        )

        Log.d(TAG, "AI 피팅 요청 clothesIdList: $clothesIdList")

        if (clothesIdList.all { it == -1 }) {
            Toast.makeText(requireContext(), "최소 1개 이상의 의류를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // ⭐ ViewModel을 통해 비동기 요청
        viewModel.requestAiFitting(clothesIdList)
    }

    /**
     * ⭐ AI 가상 피팅 결과 표시
     */
    private fun showAiFittingResult(imageUrl: String) {
        Log.d(TAG, "✅ AI 피팅 결과 표시: $imageUrl")

        val finalUrl = if (imageUrl.startsWith("http")) {
            imageUrl
        } else {
            "${ApplicationClass.API_BASE_URL}$imageUrl"
        }

        // ✅ 먼저 이미지를 로드
        Glide.with(this)
            .load(finalUrl)
            .placeholder(R.drawable.bg_slot_empty)
            .error(R.drawable.error)
            .centerInside()
            .into(binding.ivAiFittingResult)

        // ✅ 레이아웃 표시 (비디오는 이미 숨겨짐)
        binding.layoutAiFitting.visibility = View.VISIBLE
        binding.ivAiFittingResult.visibility = View.VISIBLE
        binding.vvAiLoading.visibility = View.GONE

        Log.d(TAG, "✅ 가상피팅 이미지 표시 완료")
    }

    private fun hideAiFittingResult() {
        Log.d(TAG, "AI 피팅 결과 숨기기")

        binding.layoutAiFitting.visibility = View.GONE

        Glide.with(this).clear(binding.ivAiFittingResult)
        binding.ivAiFittingResult.setImageDrawable(null)

        // ⭐ 단계도 초기화
        viewModel.clearAiFittingResult()
    }

    override fun onPause() {
        super.onPause()
        binding.vvAiLoading.stopPlayback()
    }
}
