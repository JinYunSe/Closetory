package com.ssafy.closetory.homeActivity.styling

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentStylingBinding
import com.ssafy.closetory.dto.ClothItemDto
import com.ssafy.closetory.homeActivity.adpter.ClothAdapter

private const val TAG = "StylingFragment_싸피"

class StylingFragment :
    BaseFragment<FragmentStylingBinding>(
        FragmentStylingBinding::bind,
        R.layout.fragment_styling
    ) {

    // ViewModel 초기화
    private val viewModel: StylingViewModel by viewModels()

    // 각 타입별 어댑터
    private lateinit var topAdapter: ClothAdapter
    private lateinit var bottomAdapter: ClothAdapter
    private lateinit var outerAdapter: ClothAdapter
    private lateinit var accAdapter: ClothAdapter
    private lateinit var bagAdapter: ClothAdapter
    private lateinit var shoeAdapter: ClothAdapter

    // 현재 슬롯에 선택된 아이템들을 저장
    // 순서: Top, Bottom, Shoes, Outer, Accessory, Bag
    private val selectedSlots = mutableMapOf<String, ClothItemDto?>(
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

        // 초기 데이터 로드 (보유한 옷만)
        Log.d(TAG, "📡 서버에서 의류 데이터 로드 시작 (onlyMine=false)")
        viewModel.loadClothItems(onlyMine = false)
    }

    /**
     * RecyclerView 초기화 및 설정
     */
    private fun setupRecyclerViews() {
        // 상의 어댑터
        topAdapter = ClothAdapter().apply {
            onItemClickListener = { item ->
                Log.d(TAG, "상의 클릭: clothesId=${item.clothesId}")
                addItemToSlot("TOP", item, binding.ivSlotTop, binding.btnRemoveTop)
            }
        }
        binding.lvTopCloth.adapter = topAdapter

        // 하의 어댑터
        bottomAdapter = ClothAdapter().apply {
            onItemClickListener = { item ->
                Log.d(TAG, "하의 클릭: clothesId=${item.clothesId}")
                addItemToSlot("BOTTOM", item, binding.ivSlotBottom, binding.btnRemoveBottom)
            }
        }
        binding.lvBottomCloth.adapter = bottomAdapter

        // 아우터 어댑터
        outerAdapter = ClothAdapter().apply {
            onItemClickListener = { item ->
                Log.d(TAG, "아우터 클릭: clothesId=${item.clothesId}")
                addItemToSlot("OUTER", item, binding.ivSlotOuter, binding.btnRemoveOuter)
            }
        }
        binding.lvOuter.adapter = outerAdapter

        // 액세서리 어댑터
        accAdapter = ClothAdapter().apply {
            onItemClickListener = { item ->
                Log.d(TAG, "액세서리 클릭: clothesId=${item.clothesId}")
                addItemToSlot("ACC", item, binding.ivSlotAcc, binding.btnRemoveAcc)
            }
        }
        binding.lvAccCloth.adapter = accAdapter

        // 가방 어댑터
        bagAdapter = ClothAdapter().apply {
            onItemClickListener = { item ->
                Log.d(TAG, "가방 클릭: clothesId=${item.clothesId}")
                addItemToSlot("BAG", item, binding.ivSlotBag, binding.btnRemoveBag)
            }
        }
        binding.lvBagCloth.adapter = bagAdapter

        // 신발 어댑터
        shoeAdapter = ClothAdapter().apply {
            onItemClickListener = { item ->
                Log.d(TAG, "신발 클릭: clothesId=${item.clothesId}")
                addItemToSlot("SHOES", item, binding.ivSlotShoes, binding.btnRemoveShoes)
            }
        }
        binding.lvShoesCloth.adapter = shoeAdapter
    }

    /**
     * 버튼 클릭 리스너 설정
     */
    private fun setupButtons() {
        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // AI 가상 피팅 버튼
        binding.btnAiVirtualfitting.setOnClickListener {
            requestAiFitting()
            Toast.makeText(requireContext(), "AI 가상 생성중입니다", Toast.LENGTH_LONG).show()
        }

        binding.btnCloseAiFitting.setOnClickListener {
            hideAiFittingResult()
        }

        // 보유한 옷만 보기 스위치
        binding.switchSwitchOwnedOnly.setOnCheckedChangeListener { _, isChecked ->
            updateSwitchText(isChecked)
            viewModel.loadClothItems(onlyMine = isChecked)
        }

        // 등록 버튼 (룩 저장)
        binding.btnStylingRegister.setOnClickListener {
            saveLook()
        }

        // 초기화 버튼 (모든 슬롯 비우기)
        binding.btnStylingReset.setOnClickListener {
            clearAllSlots()
        }

        // 각 슬롯의 삭제 버튼 설정
        setupRemoveButtons()
    }

    /**
     * 스위치 텍스트 업데이트
     */
    private fun updateSwitchText(isOwnedOnly: Boolean) {
        binding.tvSwitchOwnedOnly.text = if (isOwnedOnly) "내 옷만" else "모든 옷"
    }

    /**
     * 슬롯별 삭제 버튼 설정
     */
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

    /**
     * ViewModel LiveData 관찰
     */
    private fun observeViewModel() {
        // 의류 데이터 관찰
        viewModel.closetData.observe(viewLifecycleOwner) { data ->
            if (data == null) {
                Log.e(TAG, "데이터가 NULL입니다!")
                return@observe
            }

            Log.d(TAG, "의류 데이터 수신: $data")
            Log.d(TAG, "상의 개수: ${data.topClothes?.size ?: 0}")
            Log.d(TAG, "하의 개수: ${data.bottomClothes?.size ?: 0}")
            Log.d(TAG, "아우터 개수: ${data.outerClothes?.size ?: 0}")
            Log.d(TAG, "액세서리 개수: ${data.accessories?.size ?: 0}")
            Log.d(TAG, "가방 개수: ${data.bags?.size ?: 0}")
            Log.d(TAG, "신발 개수: ${data.shoes?.size ?: 0}")

            // 각 카테고리별 데이터 제출
            topAdapter.submitList(data.topClothes ?: emptyList())
            bottomAdapter.submitList(data.bottomClothes ?: emptyList())
            outerAdapter.submitList(data.outerClothes ?: emptyList())
            accAdapter.submitList(data.accessories ?: emptyList())
            bagAdapter.submitList(data.bags ?: emptyList())
            shoeAdapter.submitList(data.shoes ?: emptyList())

            // 데이터 제출 완료 로그
            Log.d(TAG, "모든 Adapter에 데이터 제출 완료")
        }

        // AI 가상 피팅 결과 관찰 추가
        viewModel.aiImageUrl.observe(viewLifecycleOwner) { imageUrl ->
            if (imageUrl != null) {
                showAiFittingResult(imageUrl)
            }
        }

        // 로딩 상태 관찰
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressAiFitting.visibility = View.VISIBLE
            } else {
                binding.progressAiFitting.visibility = View.GONE
            }

            binding.btnStylingRegister.isEnabled = !isLoading
            binding.btnStylingReset.isEnabled = !isLoading
            binding.btnAiVirtualfitting.isEnabled = !isLoading
        }

        // 에러 메시지 관찰
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // 성공 메시지 관찰
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                // 성공 시 모든 슬롯 초기화
                clearAllSlots()
            }
        }
    }

    /**
     * 슬롯에 아이템 추가 (클릭 시 올라가기)
     */
//    private fun addItemToSlot(slotType: String, item: ClothItemDto, imageView: ImageView, removeButton: View) {
// //        Log.d(TAG, "addItemToSlot - slotType: $slotType, itemId: ${item.clothesId}")
// //
// //        // 슬롯에 저장
// //        selectedSlots[slotType] = item
// //
// //        // 이미지 로드
// //        val imageUrl = "${ApplicationClass.SERVER_URL}${item.photoUrl}"
// //        Glide.with(this)
// //            .load(imageUrl)
// //            .placeholder(R.drawable.bg_slot_empty)
// //            .error(R.drawable.bg_slot_empty)
// //            .centerInside()
// //            .into(imageView)
// //
// //        // 배경 제거 (이미지만 보이게)
// //        imageView.background = null
// //
// //        // 삭제 버튼 표시
// //        removeButton.visibility = View.VISIBLE
// //
// //        Log.d(TAG, "슬롯 업데이트 완료: $slotType")
// //    }
    private fun addItemToSlot(slotType: String, item: ClothItemDto, imageView: ImageView, removeButton: View) {
        Log.d(TAG, "=== addItemToSlot 호출 ===")
        Log.d(TAG, "slotType: $slotType")
        Log.d(TAG, "clothesId: ${item.clothesId}")
        Log.d(TAG, "photoUrl: ${item.photoUrl}")
        Log.d(TAG, "SERVER_URL: ${ApplicationClass.SERVER_URL}")

        selectedSlots[slotType] = item

//        val imageUrl = "${ApplicationClass.SERVER_URL}${item.photoUrl}"
//        Log.d(TAG, "최종 imageUrl: $imageUrl")
        val imageUrl = if (item.photoUrl.startsWith("http")) {
            item.photoUrl // 이미 완전한 URL
        } else {
            "${ApplicationClass.SERVER_URL}${item.photoUrl}" // 상대 경로
        }

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.bg_slot_empty)
            .error(R.drawable.bg_slot_empty)
            .centerInside()
            .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                override fun onLoadFailed(
                    e: com.bumptech.glide.load.engine.GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e(TAG, "슬롯 이미지 로딩 실패!")
                    Log.e(TAG, "URL: $model")
                    Log.e(TAG, "에러: ${e?.message}")
                    e?.printStackTrace()
                    return false
                }

                override fun onResourceReady(
                    resource: android.graphics.drawable.Drawable,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d(TAG, "✅ 슬롯 이미지 로딩 성공!")
                    return false
                }
            })
            .into(imageView)

        imageView.background = null
        removeButton.visibility = View.VISIBLE
    }

    /**
     * 슬롯에서 아이템 제거 (X 버튼 클릭 시 내려가기)
     */
    private fun removeItemFromSlot(slotType: String, imageView: ImageView, removeButton: View) {
        Log.d(TAG, "removeItemFromSlot - slotType: $slotType")

        // 슬롯에서 제거
        selectedSlots[slotType] = null

        // 이미지 초기화
        Glide.with(this).clear(imageView)
        imageView.setImageDrawable(null)
        imageView.setBackgroundResource(R.drawable.bg_slot_empty)

        // 삭제 버튼 숨기기
        removeButton.visibility = View.GONE

        Log.d(TAG, "슬롯 제거 완료: $slotType")
    }

    /**
     * 모든 슬롯 초기화 (초기화 버튼 클릭 시)
     */
    private fun clearAllSlots() {
        Log.d(TAG, "clearAllSlots 호출")

        removeItemFromSlot("TOP", binding.ivSlotTop, binding.btnRemoveTop)
        removeItemFromSlot("BOTTOM", binding.ivSlotBottom, binding.btnRemoveBottom)
        removeItemFromSlot("OUTER", binding.ivSlotOuter, binding.btnRemoveOuter)
        removeItemFromSlot("ACC", binding.ivSlotAcc, binding.btnRemoveAcc)
        removeItemFromSlot("BAG", binding.ivSlotBag, binding.btnRemoveBag)
        removeItemFromSlot("SHOES", binding.ivSlotShoes, binding.btnRemoveShoes)

        Toast.makeText(requireContext(), "코디가 초기화되었습니다", Toast.LENGTH_SHORT).show()
    }

    /**
     * 룩 저장 (등록 버튼 클릭 시 서버로 전송)
     * 순서: Top, Bottom, Shoes, Outer, Accessory, Bag
     */
    private fun saveLook() {
        Log.d(TAG, "saveLook 호출")

        // 선택된 아이템 ID 리스트 생성 (순서 중요!)
        val clothIdList = listOf(
            selectedSlots["TOP"]?.clothesId ?: -1, // Top
            selectedSlots["BOTTOM"]?.clothesId ?: -1, // Bottom
            selectedSlots["SHOES"]?.clothesId ?: -1, // Shoes
            selectedSlots["OUTER"]?.clothesId ?: -1, // Outer
            selectedSlots["ACC"]?.clothesId ?: -1, // Accessory
            selectedSlots["BAG"]?.clothesId ?: -1 // Bag
        )

        Log.d(TAG, "전송할 clothesIdList: $clothIdList")

        // 최소 1개 이상 선택 확인
        if (clothIdList.all { it == -1 }) {
            Toast.makeText(requireContext(), "최소 1개 이상의 의류를 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        // ViewModel을 통해 서버로 전송
        viewModel.saveLook(clothIdList)
    }

    // AI가상피팅
    private fun requestAiFitting() {
        Log.d(TAG, "requestAiFitting 호출")

        val clothIdList = listOf(
            selectedSlots["TOP"]?.clothesId ?: -1,
            selectedSlots["BOTTOM"]?.clothesId ?: -1,
            selectedSlots["SHOES"]?.clothesId ?: -1,
            selectedSlots["OUTER"]?.clothesId ?: -1,
            selectedSlots["ACC"]?.clothesId ?: -1,
            selectedSlots["BAG"]?.clothesId ?: -1
        )

        Log.d(TAG, "AI 피팅 요청 clothIdList: $clothIdList")

        // 최소 1개 이상 선택 확인
        if (clothIdList.all { it == -1 }) {
            Toast.makeText(requireContext(), "최소 1개 이상의 의류를 선택해주세요.", Toast.LENGTH_SHORT)
            return
        }

        // ViewModel을 통해 AI피팅 요청
        viewModel.requestAiFitting(clothIdList)
    }

    // AI 가상 피팅 결과 표시

    private fun showAiFittingResult(imageUrl: String) {
        Log.d(TAG, "AI 피팅 결과 표시: $imageUrl")

        // URL 처리 (http로 시작하면 그대로, 아니면 서버 URL 붙이기)
        val finalUrl = if (imageUrl.startsWith("http")) {
            imageUrl
        } else {
            "${ApplicationClass.SERVER_URL}$imageUrl"
        }

        // AI 이미지 로딩
        Glide.with(this)
            .load(finalUrl)
            .placeholder(R.drawable.bg_slot_empty)
            .error(R.drawable.error)
            .centerInside()
            .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                override fun onLoadFailed(
                    e: com.bumptech.glide.load.engine.GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e(TAG, "AI 이미지 로딩 실패: ${e?.message}")
                    Toast.makeText(requireContext(), "AI 이미지 로딩에 실패했습니다", Toast.LENGTH_SHORT).show()
                    return false
                }

                override fun onResourceReady(
                    resource: android.graphics.drawable.Drawable,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d(TAG, "✅ AI 이미지 로딩 성공")
                    return false
                }
            })
            .into(binding.ivAiFittingResult)

        // AI 레이어 표시
        binding.layoutAiFitting.visibility = View.VISIBLE
    }

    private fun hideAiFittingResult() {
        Log.d(TAG, "AI 피팅 결과 숨기기")

        binding.layoutAiFitting.visibility = View.GONE

        // 이미지 메모리 해제
        Glide.with(this).clear(binding.ivAiFittingResult)
        binding.ivAiFittingResult.setImageDrawable(null)
    }
}
