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
import com.ssafy.closetory.homeActivity.mypage.MyPageViewModel

private const val TAG = "StylingFragment"

class StylingFragment :
    BaseFragment<FragmentStylingBinding>(
        FragmentStylingBinding::bind,
        R.layout.fragment_styling
    ) {

    // Activity 스코프 ViewModel
    private val viewModel: StylingViewModel by activityViewModels()
    private val myPageViewModel: MyPageViewModel by activityViewModels()

    private lateinit var topAdapter: ClothesAdapter
    private lateinit var bottomAdapter: ClothesAdapter
    private lateinit var outerAdapter: ClothesAdapter
    private lateinit var accAdapter: ClothesAdapter
    private lateinit var bagAdapter: ClothesAdapter
    private lateinit var shoeAdapter: ClothesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "StylingFragment onViewCreated 시작")

        setupRecyclerViews()
        setupButtons()
        observeViewModel()
        setupBackPressHandler()

        viewModel.loadClothItems(onlyMine = false)
    }

    private fun setupRecyclerViews() {
        // 상의 (lv_top_cloth)
        topAdapter = ClothesAdapter().apply {
            onItemClick = { item ->
                Log.d(TAG, "상의 클릭: clothesId=${item.clothesId}")
                addItemToSlot("TOP", item, binding.ivSlotTop, binding.btnRemoveTop)
                updateStageAfterSelection()
            }
        }
        binding.lvTopCloth.adapter = topAdapter

        // 하의 (lv_bottom_cloth)
        bottomAdapter = ClothesAdapter().apply {
            onItemClick = { item ->
                Log.d(TAG, "하의 클릭: clothesId=${item.clothesId}")
                addItemToSlot("BOTTOM", item, binding.ivSlotBottom, binding.btnRemoveBottom)
                updateStageAfterSelection()
            }
        }
        binding.lvBottomCloth.adapter = bottomAdapter

        // 아우터 (lv_outer)
        outerAdapter = ClothesAdapter().apply {
            onItemClick = { item ->
                Log.d(TAG, "아우터 클릭: clothesId=${item.clothesId}")
                addItemToSlot("OUTER", item, binding.ivSlotOuter, binding.btnRemoveOuter)
                updateStageAfterSelection()
            }
        }
        binding.lvOuter.adapter = outerAdapter

        // 소품류 (lv_acc_cloth)
        accAdapter = ClothesAdapter().apply {
            onItemClick = { item ->
                Log.d(TAG, "소품류 클릭: clothesId=${item.clothesId}")
                addItemToSlot("ACCESSORIES", item, binding.ivSlotAcc, binding.btnRemoveAcc)
                updateStageAfterSelection()
            }
        }
        binding.lvAccCloth.adapter = accAdapter

        // 가방 (lv_bag_cloth)
        bagAdapter = ClothesAdapter().apply {
            onItemClick = { item ->
                Log.d(TAG, "가방 클릭: clothesId=${item.clothesId}")
                addItemToSlot("BAG", item, binding.ivSlotBag, binding.btnRemoveBag)
                updateStageAfterSelection()
            }
        }
        binding.lvBagCloth.adapter = bagAdapter

        // 신발 (lv_shoes_cloth)
        shoeAdapter = ClothesAdapter().apply {
            onItemClick = { item ->
                Log.d(TAG, "신발 클릭: clothesId=${item.clothesId}")
                addItemToSlot("SHOES", item, binding.ivSlotShoes, binding.btnRemoveShoes)
                updateStageAfterSelection()
            }
        }
        binding.lvShoesCloth.adapter = shoeAdapter
    }

    private fun setupButtons() {
        // 뒤로가기 (btn_back)
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 🎯 메인 버튼 (btn_styling_register): 상태에 따라 동작 변경
        binding.btnStylingRegister.setOnClickListener {
            handleMainButtonClick()
        }

        // AI 피팅 결과 닫기 (btn_close_ai_fitting)
        binding.btnCloseAiFitting.setOnClickListener {
            hideAiFittingResult()
        }

        // 스위치 (switch_switch_owned_only)
        binding.switchSwitchOwnedOnly.setOnCheckedChangeListener { _, isChecked ->
            updateSwitchText(isChecked)
            viewModel.loadClothItems(onlyMine = isChecked)
        }

        // 초기화 버튼 (btn_styling_reset)
        binding.btnStylingReset.setOnClickListener {
            showResetConfirmDialog()
        }

        setupRemoveButtons()
    }

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
     * 🎯 메인 버튼 클릭 핸들러 - 상태별 동작
     */
    private fun handleMainButtonClick() {
        val stage = viewModel.stage.value ?: StylingStage.SELECTING
        val isLoading = viewModel.isLoading.value == true

        // 로딩 중이면 클릭 무시
        if (isLoading) {
            Log.d(TAG, "⏳ 로딩 중이라 클릭 무시")
            return
        }

        when (stage) {
            StylingStage.SELECTING -> {
                Toast.makeText(requireContext(), "최소 1개 이상의 의류를 선택해 주세요.", Toast.LENGTH_SHORT).show()
            }

            StylingStage.FITTING_READY -> {
                // 가상피팅 요청
                requestAiFitting()
            }

            StylingStage.FITTING_DONE -> {
                // 등록 (저장)
                saveLook()
            }

            StylingStage.SAVED -> {
                // 🆕 저장 완료 후 - 코디저장소로 이동
                navigateToLookStorage()
            }
        }
    }

    /**
     * 🆕 코디저장소로 이동
     */
    private fun navigateToLookStorage() {
        try {
            // 코디저장소 Fragment로 이동
            findNavController().navigate(R.id.codyRepositoryFragment)
            viewModel.onNavigatedToLookStorage()

            Log.d(TAG, "🏪 코디저장소로 이동")
        } catch (e: Exception) {
            Log.e(TAG, "코디저장소 이동 실패", e)
            Toast.makeText(requireContext(), "코디 저장소로 이동할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // VideoView 애니메이션 제어 (layout_ai_loading)
    private fun updateVideoAnimation(isLoading: Boolean) {
        if (isLoading) {
            binding.vvAiLoading.visibility = View.VISIBLE
            Glide.with(this)
                .asGif()
                .load(R.raw.vv_ai_fitting_progress)
                .into(binding.vvAiLoading)
        } else {
            binding.vvAiLoading.visibility = View.GONE
            Glide.with(this).clear(binding.vvAiLoading)
        }
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
            removeItemFromSlot("ACCESSORIES", binding.ivSlotAcc, binding.btnRemoveAcc)
        }

        binding.btnRemoveBag.setOnClickListener {
            removeItemFromSlot("BAG", binding.ivSlotBag, binding.btnRemoveBag)
        }

        binding.btnRemoveShoes.setOnClickListener {
            removeItemFromSlot("SHOES", binding.ivSlotShoes, binding.btnRemoveShoes)
        }
    }

    private fun updateSwitchText(isChecked: Boolean) {
        binding.tvSwitchOwnedOnly.text = if (isChecked) "내 옷만" else "모든 옷"
    }

    /**
     * ViewModel 관찰
     */
    private fun observeViewModel() {
        // 의류 데이터 관찰 (ClosetResponse 구조 맞춤)
        viewModel.closetData.observe(viewLifecycleOwner) { data ->
            data?.let {
                Log.d(TAG, "closetData 수신")
                topAdapter.submitList(it.topClothes ?: emptyList())
                bottomAdapter.submitList(it.bottomClothes ?: emptyList())
                outerAdapter.submitList(it.outerClothes ?: emptyList())
                accAdapter.submitList(it.accessories ?: emptyList())
                bagAdapter.submitList(it.bags ?: emptyList())
                shoeAdapter.submitList(it.shoes ?: emptyList())
            }
        }

        // 로딩 상태 관찰
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d(TAG, "⏳ isLoading 변경: $isLoading")
            updateMainButton()
            updateVideoAnimation(isLoading)
        }

        // 단계 상태 관찰
        viewModel.stage.observe(viewLifecycleOwner) { stage ->
            Log.d(TAG, "📍 stage 변경: $stage")
            updateMainButton()

            // SAVED 상태일 때 슬롯 초기화 (UI만)
            if (stage == StylingStage.SAVED) {
                clearAllSlotsUI()
            }
        }

        // AI 가상 피팅 결과 관찰
        viewModel.aiPhotoUrl.observe(viewLifecycleOwner) { photoUrl ->
            if (photoUrl != null) {
                Log.d(TAG, "AiphotoUrl 수신: $photoUrl")
                showAiFittingResult(photoUrl)
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

                // ✅ 등록(저장) 성공하면 결과 팝업 닫기
                // (문구는 프로젝트에 맞춰 contains 조건만 조절)
                if (it.contains("저장") || it.contains("등록") || it.contains("코디")) {
                    hideAiFittingResult()
                }

                viewModel.clearSuccessMessage()
            }
        }

        // 🆕 코디저장소 이동 관찰
        viewModel.navigateToLookStorage.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                navigateToLookStorage()
            }
        }
    }

    /**
     * 🎯 메인 버튼 업데이트 (상태별)
     */
    private fun updateMainButton() {
        val stage = viewModel.stage.value ?: StylingStage.SELECTING
        val isLoading = viewModel.isLoading.value == true

        when (stage) {
            StylingStage.SELECTING -> {
                binding.btnStylingRegister.text = "AI 가상피팅"
                binding.btnStylingRegister.isEnabled = false
                binding.btnStylingRegister.alpha = 0.6f
            }

            StylingStage.FITTING_READY -> {
                if (isLoading) {
                    binding.btnStylingRegister.text = "가상피팅중"
                    binding.btnStylingRegister.isEnabled = false
                    binding.btnStylingRegister.alpha = 0.5f
                } else {
                    binding.btnStylingRegister.text = "AI 가상피팅"
                    binding.btnStylingRegister.isEnabled = true
                    binding.btnStylingRegister.alpha = 1.0f
                }
            }

            StylingStage.FITTING_DONE -> {
                if (isLoading) {
                    binding.btnStylingRegister.text = "등록중"
                    binding.btnStylingRegister.isEnabled = false
                    binding.btnStylingRegister.alpha = 0.5f
                } else {
                    binding.btnStylingRegister.text = "등록"
                    binding.btnStylingRegister.isEnabled = true
                    binding.btnStylingRegister.alpha = 1.0f
                }
            }

            StylingStage.SAVED -> {
                // 🆕 저장 완료 상태 - 코디 저장소 가기
                binding.btnStylingRegister.text = "📦 코디 저장소 가기"
                binding.btnStylingRegister.isEnabled = true
                binding.btnStylingRegister.alpha = 1.0f
            }
        }
    }

    /**
     * 옷 선택 후 단계 업데이트
     */
    private fun updateStageAfterSelection() {
        val hasSelection = viewModel.selectedSlots.values.any { it != null }
        viewModel.updateStageAfterSelection(hasSelection)
    }

    /**
     * 슬롯에 아이템 추가
     */
    private fun addItemToSlot(slotType: String, item: ClothesItemDto, imageView: ImageView, removeButton: View) {
        Log.d(TAG, "addItemToSlot: $slotType, clothesId=${item.clothesId}")

        // ViewModel에 저장
        viewModel.selectedSlots[slotType] = item

        val photoUrl = if (item.photoUrl.startsWith("http")) {
            item.photoUrl
        } else {
            "${ApplicationClass.API_BASE_URL}${item.photoUrl}"
        }

        Glide.with(this)
            .load(photoUrl)
            .placeholder(R.drawable.bg_slot_empty)
            .error(R.drawable.bg_slot_empty)
            .centerInside()
            .into(imageView)

        imageView.background = null
        removeButton.visibility = View.VISIBLE
    }

    /**
     * 슬롯에서 아이템 제거
     */
    private fun removeItemFromSlot(slotType: String, imageView: ImageView, removeButton: View) {
        Log.d(TAG, "removeItemFromSlot: $slotType")

        // ViewModel에서 제거
        viewModel.selectedSlots[slotType] = null

        Glide.with(this).clear(imageView)
        imageView.setImageDrawable(null)
        imageView.setBackgroundResource(R.drawable.bg_slot_empty)

        removeButton.visibility = View.GONE

        updateStageAfterSelection()
    }

    /**
     * 전체 슬롯 초기화
     */
    private fun clearAllSlots() {
        Log.d(TAG, "clearAllSlots 호출")

        removeItemFromSlot("TOP", binding.ivSlotTop, binding.btnRemoveTop)
        removeItemFromSlot("BOTTOM", binding.ivSlotBottom, binding.btnRemoveBottom)
        removeItemFromSlot("OUTER", binding.ivSlotOuter, binding.btnRemoveOuter)
        removeItemFromSlot("ACCESSORIES", binding.ivSlotAcc, binding.btnRemoveAcc)
        removeItemFromSlot("BAG", binding.ivSlotBag, binding.btnRemoveBag)
        removeItemFromSlot("SHOES", binding.ivSlotShoes, binding.btnRemoveShoes)

        hideAiFittingResult()
        viewModel.resetAll()
    }

    /**
     * 🆕 UI만 초기화 (SAVED 상태에서 사용)
     */
    private fun clearAllSlotsUI() {
        Log.d(TAG, "clearAllSlotsUI 호출 (UI만)")

        // UI만 초기화
        val slots = listOf(
            Triple("TOP", binding.ivSlotTop, binding.btnRemoveTop),
            Triple("BOTTOM", binding.ivSlotBottom, binding.btnRemoveBottom),
            Triple("OUTER", binding.ivSlotOuter, binding.btnRemoveOuter),
            Triple("ACCESSORIES", binding.ivSlotAcc, binding.btnRemoveAcc),
            Triple("BAG", binding.ivSlotBag, binding.btnRemoveBag),
            Triple("SHOES", binding.ivSlotShoes, binding.btnRemoveShoes)
        )

        slots.forEach { (_, imageView, removeButton) ->
            Glide.with(this).clear(imageView)
            imageView.setImageDrawable(null)
            imageView.setBackgroundResource(R.drawable.bg_slot_empty)
            removeButton.visibility = View.GONE
        }

        hideAiFittingResult()
    }

    /**
     * 룩 저장
     */
    private fun saveLook() {
        Log.d(TAG, "saveLook 호출")

        val clothesIdList = listOf(
            viewModel.selectedSlots["TOP"]?.clothesId ?: -1,
            viewModel.selectedSlots["BOTTOM"]?.clothesId ?: -1,
            viewModel.selectedSlots["SHOES"]?.clothesId ?: -1,
            viewModel.selectedSlots["OUTER"]?.clothesId ?: -1,
            viewModel.selectedSlots["ACCESSORIES"]?.clothesId ?: -1,
            viewModel.selectedSlots["BAG"]?.clothesId ?: -1
        )

        Log.d(TAG, "전송할 clothesIdList: $clothesIdList")

        // TOP, BOTTOM, SHOES는 필수
        if (clothesIdList[0] == -1 || clothesIdList[1] == -1 || clothesIdList[2] == -1) {
            Toast.makeText(requireContext(), "상의, 하의, 신발은 필수로 선택해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (clothesIdList.all { it == -1 }) {
            Toast.makeText(requireContext(), "최소 1개 이상의 의류를 선택해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveLook(clothesIdList)
    }

    /**
     * AI 가상피팅 요청
     */
    private fun requestAiFitting() {
        Log.d(TAG, "requestAiFitting 호출")

        if (!ensureBodyPhotoOrNavigate()) return

        val clothesIdList = listOf(
            viewModel.selectedSlots["TOP"]?.clothesId ?: -1,
            viewModel.selectedSlots["BOTTOM"]?.clothesId ?: -1,
            viewModel.selectedSlots["SHOES"]?.clothesId ?: -1,
            viewModel.selectedSlots["OUTER"]?.clothesId ?: -1,
            viewModel.selectedSlots["ACCESSORIES"]?.clothesId ?: -1,
            viewModel.selectedSlots["BAG"]?.clothesId ?: -1
        )

        Log.d(TAG, "AI 피팅 요청 clothesIdList 요소: $clothesIdList")

        // TOP, BOTTOM, SHOES는 필수
        if (clothesIdList[0] == -1 || clothesIdList[1] == -1 || clothesIdList[2] == -1) {
            Toast.makeText(requireContext(), "상의, 하의, 신발은 필수로 선택해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (clothesIdList.all { it == -1 }) {
            Toast.makeText(requireContext(), "최소 1개 이상의 의류를 선택해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.requestAiFitting(clothesIdList)
    }

    private fun ensureBodyPhotoOrNavigate(): Boolean {
        val profile = myPageViewModel.getCachedUserProfile()
        val cachedBodyPhotoUrl = profile?.bodyPhotoUrl
        val storedBodyPhotoUrl = ApplicationClass.sharedPreferences.getBodyPhotoUrl()
        val bodyPhotoUrl = cachedBodyPhotoUrl ?: storedBodyPhotoUrl

        if (profile == null && !bodyPhotoUrl.isNullOrBlank()) {
            return true
        }

        if (profile == null) {
            val userId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID) ?: -1
            if (userId != -1) {
                myPageViewModel.loadUserProfile(userId)
            }
            Toast.makeText(requireContext(), "프로필 정보를 불러오는 중입니다. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!bodyPhotoUrl.isNullOrBlank()) return true

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("전신 사진 필요")
            .setMessage("AI 가상 피팅을 위해 전신 프로필 사진이 필요합니다.\n내 정보에서 등록해 주세요.")
            .setPositiveButton("내 정보로 이동") { _, _ ->
                findNavController().navigate(R.id.editProfileFragment)
            }
            .setNegativeButton("취소", null)
            .show()

        return false
    }

    /**
     * AI 가상 피팅 결과 표시 (layout_ai_fitting)
     */
    private fun showAiFittingResult(photoUrl: String) {
        Log.d(TAG, "AI 피팅 결과 표시: $photoUrl")

        val finalUrl = if (photoUrl.startsWith("http")) {
            photoUrl
        } else {
            "${ApplicationClass.API_BASE_URL}$photoUrl"
        }

        // 팝업 표시 (layout_ai_fitting, iv_ai_fitting_result)
        binding.layoutAiFitting.visibility = View.VISIBLE
        binding.ivAiFittingResult.visibility = View.VISIBLE

        Glide.with(this)
            .load(finalUrl)
            .placeholder(R.drawable.bg_slot_empty)
            .error(R.drawable.error)
            .centerInside()
            .into(binding.ivAiFittingResult)

        Log.d(TAG, "가상피팅 이미지 표시 완료")
    }

    /**
     * 가상피팅 결과 숨기기
     */
    private fun hideAiFittingResult() {
        Log.d(TAG, "AI 피팅 결과 숨기기")

        binding.layoutAiFitting.visibility = View.GONE
        binding.ivAiFittingResult.visibility = View.GONE

        Glide.with(this).clear(binding.ivAiFittingResult)
        binding.ivAiFittingResult.setImageDrawable(null)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        viewModel.resetAfterFittingDoneIfNeeded()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Fragment 파괴 시에는 초기화하지 않음
    }
}
