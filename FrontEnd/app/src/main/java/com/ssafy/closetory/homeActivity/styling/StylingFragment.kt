package com.ssafy.closetory.homeActivity.styling

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
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

    // ✅ ClosetFragment 방식: 탭 + 단일 Grid 리스트
    private val clothAdapter = ClothesAdapter()

    private var cachedTop: List<ClothesItemDto> = emptyList()
    private var cachedBottom: List<ClothesItemDto> = emptyList()
    private var cachedOuter: List<ClothesItemDto> = emptyList()
    private var cachedShoes: List<ClothesItemDto> = emptyList()
    private var cachedBags: List<ClothesItemDto> = emptyList()
    private var cachedAcc: List<ClothesItemDto> = emptyList()

    // ✅ 스위치 상태 (내 옷만 보기)
    private var checkedOnlyMyCloth: Boolean = false
    private var suppressSwitchListener: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "StylingFragment onViewCreated 시작")

        setupClosetRecyclerView()
        setupTabs()
        setupButtons()
        observeViewModel()
        setupBackPressHandler()

        restoreSlotsFromViewModel()
        viewModel.syncStageWithCurrentState()

        // 최초 로드: 전체 옷
        viewModel.loadClothItems(onlyMine = false)
    }

    // ---------------------------------------------------------
    // Closet 리스트(Grid) + 탭
    // ---------------------------------------------------------
    private fun setupClosetRecyclerView() {
        binding.closetList.glCloset.apply {
            adapter = clothAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
            setHasFixedSize(true)
        }

        // ✅ 클릭 시: "상세 이동"이 아니라 "슬롯에 추가"
        clothAdapter.onItemClick = { item ->
            when (binding.closetList.tabCloset.selectedTabPosition) {
                0 -> addItemToSlot("TOP", item, binding.ivSlotTop, binding.btnRemoveTop)
                1 -> addItemToSlot("BOTTOM", item, binding.ivSlotBottom, binding.btnRemoveBottom)
                2 -> addItemToSlot("OUTER", item, binding.ivSlotOuter, binding.btnRemoveOuter)
                3 -> addItemToSlot("SHOES", item, binding.ivSlotShoes, binding.btnRemoveShoes)
                4 -> addItemToSlot("BAG", item, binding.ivSlotBag, binding.btnRemoveBag)
                5 -> addItemToSlot("ACCESSORIES", item, binding.ivSlotAcc, binding.btnRemoveAcc)
            }
            updateStageAfterSelection()
        }
    }

    private fun setupTabs() {
        binding.closetList.tabCloset.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                applyTabItems()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })

        // 첫 탭 선택
        binding.closetList.tabCloset.getTabAt(0)?.select()
    }

    private fun applyTabItems() {
        val position = binding.closetList.tabCloset.selectedTabPosition
        val list = when (position) {
            0 -> cachedTop
            1 -> cachedBottom
            2 -> cachedOuter
            3 -> cachedShoes
            4 -> cachedBags
            5 -> cachedAcc
            else -> emptyList()
        }

        clothAdapter.submitList(list)

        val isEmpty = list.isEmpty()
        binding.closetList.glCloset.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.closetList.tvEmptyCloset.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    // ---------------------------------------------------------
    // Buttons / Switch / Back
    // ---------------------------------------------------------
    private fun setupButtons() {
        // 뒤로가기 (btn_back)
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 메인 버튼 (btn_styling_register): 상태에 따라 동작 변경
        binding.btnStylingRegister.setOnClickListener {
            handleMainButtonClick()
        }

        // AI 피팅 결과 닫기 (btn_close_ai_fitting)
        binding.btnCloseAiFitting.setOnClickListener {
            hideAiFittingResult()
        }

        // 초기화 버튼
        binding.btnStylingReset.setOnClickListener {
            showResetConfirmDialog()
        }

        setupOwnedOnlySwitch()
        setupRemoveButtons()
    }

    // ✅ 내 옷만 보기 스위치 (Styling XML의 tv_switch_owned_only + switch_switch_owned_only 사용)
    private fun setupOwnedOnlySwitch() {
        suppressSwitchListener = true
        binding.switchSwitchOwnedOnly.isChecked = checkedOnlyMyCloth
        suppressSwitchListener = false

        binding.switchSwitchOwnedOnly.setOnCheckedChangeListener { _, isChecked ->
            if (suppressSwitchListener) return@setOnCheckedChangeListener

            checkedOnlyMyCloth = isChecked

            // ✅ 내 옷만/전체 옷 로드
            viewModel.loadClothItems(onlyMine = isChecked)
        }
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
                .setPositiveButton("초기화") { _, _ -> clearAllSlots() }
                .setNegativeButton("취소", null)
                .show()
            return
        }

        if (stage != StylingStage.SELECTING) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("초기화 확인")
                .setMessage("현재 작업 내용이 초기화됩니다.\n계속하시겠습니까?")
                .setPositiveButton("초기화") { _, _ -> clearAllSlots() }
                .setNegativeButton("취소", null)
                .show()
        } else {
            clearAllSlots()
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

    // ---------------------------------------------------------
    // ViewModel Observers
    // ---------------------------------------------------------
    private fun observeViewModel() {
        // ✅ 옷 데이터 수신 → 캐싱 → 현재 탭 렌더
        viewModel.closetData.observe(viewLifecycleOwner) { data ->
            data?.let {
                cachedTop = it.topClothes ?: emptyList()
                cachedBottom = it.bottomClothes ?: emptyList()
                cachedOuter = it.outerClothes ?: emptyList()
                cachedAcc = it.accessories ?: emptyList()
                cachedBags = it.bags ?: emptyList()
                cachedShoes = it.shoes ?: emptyList()

                applyTabItems()
            }
        }

        // 로딩 상태
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d(TAG, "⏳ isLoading 변경: $isLoading")
            updateMainButton()
            updateVideoAnimation(isLoading)
        }

        // 단계 상태
        viewModel.stage.observe(viewLifecycleOwner) { stage ->
            Log.d(TAG, "📍 stage 변경: $stage")
            updateMainButton()

            if (stage == StylingStage.SAVED) {
                clearAllSlotsUI()
            }
        }

        // AI 가상 피팅 결과
        viewModel.aiPhotoUrl.observe(viewLifecycleOwner) { photoUrl ->
            if (photoUrl != null) {
                Log.d(TAG, "AiphotoUrl 수신: $photoUrl")
                showAiFittingResult(photoUrl)
                autoSaveLookIfNeeded()
            }
        }

        // 에러 메시지
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        }

        // 성공 메시지
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()

                if (it.contains("저장") || it.contains("등록") || it.contains("코디")) {
                    hideAiFittingResult()
                }

                viewModel.clearSuccessMessage()
            }
        }

        // 코디저장소 이동
        viewModel.navigateToLookStorage.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                navigateToLookStorage()
            }
        }
    }

    // ---------------------------------------------------------
    // Main button / Stage
    // ---------------------------------------------------------
    private fun handleMainButtonClick() {
        val stage = viewModel.stage.value ?: StylingStage.SELECTING
        val isLoading = viewModel.isLoading.value == true

        if (isLoading) {
            Log.d(TAG, "⏳ 로딩 중이라 클릭 무시")
            return
        }

        when (stage) {
            StylingStage.SELECTING -> {
                Toast.makeText(requireContext(), "최소 1개 이상의 의류를 선택해 주세요.", Toast.LENGTH_SHORT).show()
            }

            StylingStage.FITTING_READY -> {
                requestAiFitting()
            }

            StylingStage.FITTING_DONE -> {
                navigateToLookStorage()
            }

            StylingStage.SAVED -> {
                navigateToLookStorage()
            }
        }
    }

    private fun updateMainButton() {
        val stage = viewModel.stage.value ?: StylingStage.SELECTING
        val isLoading = viewModel.isLoading.value == true

        when (stage) {
            StylingStage.SELECTING -> {
                binding.btnStylingRegister.text = "가상 피팅"
                binding.btnStylingRegister.isEnabled = false
                binding.btnStylingRegister.alpha = 0.6f
            }

            StylingStage.FITTING_READY -> {
                if (isLoading) {
                    binding.btnStylingRegister.text = "피팅 중"
                    binding.btnStylingRegister.isEnabled = false
                    binding.btnStylingRegister.alpha = 0.5f
                } else {
                    binding.btnStylingRegister.text = "가상 피팅"
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
                    binding.btnStylingRegister.text = "히스토리"
                    binding.btnStylingRegister.isEnabled = true
                    binding.btnStylingRegister.alpha = 1.0f
                }
            }

            StylingStage.SAVED -> {
                binding.btnStylingRegister.text = "히스토리"
                binding.btnStylingRegister.isEnabled = true
                binding.btnStylingRegister.alpha = 1.0f
            }
        }
    }

    private fun updateStageAfterSelection() {
        val hasSelection = viewModel.selectedSlots.values.any { it != null }
        viewModel.updateStageAfterSelection(hasSelection)
    }

    // ---------------------------------------------------------
    // Slots (restore / add / remove / clear)
    // ---------------------------------------------------------
    private fun restoreSlotsFromViewModel() {
        val slots = listOf(
            Triple("TOP", binding.ivSlotTop, binding.btnRemoveTop),
            Triple("BOTTOM", binding.ivSlotBottom, binding.btnRemoveBottom),
            Triple("SHOES", binding.ivSlotShoes, binding.btnRemoveShoes),
            Triple("OUTER", binding.ivSlotOuter, binding.btnRemoveOuter),
            Triple("ACCESSORIES", binding.ivSlotAcc, binding.btnRemoveAcc),
            Triple("BAG", binding.ivSlotBag, binding.btnRemoveBag)
        )

        slots.forEach { (type, imageView, removeButton) ->
            val item = viewModel.selectedSlots[type]
            if (item == null) {
                Glide.with(this).clear(imageView)
                imageView.setImageDrawable(null)
                imageView.setBackgroundResource(R.drawable.bg_slot_empty)
                removeButton.visibility = View.GONE
            } else {
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
        }
    }

    private fun addItemToSlot(slotType: String, item: ClothesItemDto, imageView: ImageView, removeButton: View) {
        Log.d(TAG, "addItemToSlot: $slotType, clothesId=${item.clothesId}")

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

    private fun removeItemFromSlot(slotType: String, imageView: ImageView, removeButton: View) {
        Log.d(TAG, "removeItemFromSlot: $slotType")

        viewModel.selectedSlots[slotType] = null

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
        removeItemFromSlot("ACCESSORIES", binding.ivSlotAcc, binding.btnRemoveAcc)
        removeItemFromSlot("BAG", binding.ivSlotBag, binding.btnRemoveBag)
        removeItemFromSlot("SHOES", binding.ivSlotShoes, binding.btnRemoveShoes)

        hideAiFittingResult()
        viewModel.resetAll()
    }

    private fun clearAllSlotsUI() {
        Log.d(TAG, "clearAllSlotsUI 호출 (UI만)")

        val slots = listOf(
            binding.ivSlotTop to binding.btnRemoveTop,
            binding.ivSlotBottom to binding.btnRemoveBottom,
            binding.ivSlotOuter to binding.btnRemoveOuter,
            binding.ivSlotAcc to binding.btnRemoveAcc,
            binding.ivSlotBag to binding.btnRemoveBag,
            binding.ivSlotShoes to binding.btnRemoveShoes
        )

        slots.forEach { (imageView, removeButton) ->
            Glide.with(this).clear(imageView)
            imageView.setImageDrawable(null)
            imageView.setBackgroundResource(R.drawable.bg_slot_empty)
            removeButton.visibility = View.GONE
        }

        hideAiFittingResult()
    }

    // ---------------------------------------------------------
    // Look save / AI fitting
    // ---------------------------------------------------------
    private fun autoSaveLookIfNeeded() {
        val clothesIdList = listOf(
            viewModel.selectedSlots["TOP"]?.clothesId ?: -1,
            viewModel.selectedSlots["BOTTOM"]?.clothesId ?: -1,
            viewModel.selectedSlots["SHOES"]?.clothesId ?: -1,
            viewModel.selectedSlots["OUTER"]?.clothesId ?: -1,
            viewModel.selectedSlots["ACCESSORIES"]?.clothesId ?: -1,
            viewModel.selectedSlots["BAG"]?.clothesId ?: -1
        )

        if (clothesIdList.all { it == -1 }) return

        Log.d(TAG, "autoSaveLook 호출: $clothesIdList")
        viewModel.saveLook(clothesIdList, autoSave = true)
    }

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

        if (!bodyPhotoUrl.isNullOrBlank()) return true

        if (profile == null) {
            val userId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID) ?: -1
            if (userId != -1) {
                myPageViewModel.loadUserProfile(userId)
            }
        }

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

    private fun showAiFittingResult(photoUrl: String) {
        Log.d(TAG, "AI 피팅 결과 표시: $photoUrl")

        val finalUrl = if (photoUrl.startsWith("http")) {
            photoUrl
        } else {
            "${ApplicationClass.API_BASE_URL}$photoUrl"
        }

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

    private fun hideAiFittingResult() {
        Log.d(TAG, "AI 피팅 결과 숨기기")

        binding.layoutAiFitting.visibility = View.GONE
        binding.ivAiFittingResult.visibility = View.GONE

        Glide.with(this).clear(binding.ivAiFittingResult)
        binding.ivAiFittingResult.setImageDrawable(null)
    }

    // Video/GIF 로딩 애니메이션 제어
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

    private fun navigateToLookStorage() {
        try {
            findNavController().navigate(R.id.codyRepositoryFragment)
            viewModel.onNavigatedToLookStorage()
            Log.d(TAG, "🏪 코디저장소로 이동")
        } catch (e: Exception) {
            Log.e(TAG, "코디저장소 이동 실패", e)
            Toast.makeText(requireContext(), "코디 저장소로 이동할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Fragment 파괴 시에는 초기화하지 않음
    }
}
