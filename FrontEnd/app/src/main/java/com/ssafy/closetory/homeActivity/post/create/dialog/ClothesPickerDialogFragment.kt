package com.ssafy.closetory.homeActivity.post.create.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.ssafy.closetory.R
import com.ssafy.closetory.databinding.FragmentClosetBinding
import com.ssafy.closetory.dto.ClosetResponse
import com.ssafy.closetory.homeActivity.adpter.ClothAdapter
import com.ssafy.closetory.homeActivity.closet.ClosetViewModel
import com.ssafy.closetory.util.ColorOptions
import com.ssafy.closetory.util.SeasonOptions
import com.ssafy.closetory.util.TagOptions

class ClothesPickerDialogFragment : DialogFragment() {

    private var _binding: FragmentClosetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ClosetViewModel by viewModels()
    private val clothAdapter = ClothAdapter()

    // ClosetFragment에서 쓰던 상태값들 그대로(최대한 유사)
    private lateinit var colorAdapter: ColorOptions.ColorAdapter
    private var currentTags: List<Int> = TagOptions.items.map { it.code }
    private var currentSeasons: List<Int> = SeasonOptions.items.map { it.code }
    private var currentColor: String? = null
    private var checkedOnlyMyCloth: Boolean = false

    // observe는 viewLifecycleOwner가 준비된 이후에만 붙이기 위해 Observer를 필드로 분리
    private val closetObserver = Observer<ClosetResponse?> { data ->
        if (data == null) return@Observer
        applyTabItems(data)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentClosetBinding.inflate(LayoutInflater.from(requireContext()))

        initRecyclerViews()
        checkSwitch()
        searchDialog()
        selectedTab()

        // 최초 조회
        runSearch()

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    // DialogFragment에서 viewLifecycleOwner가 안전하게 존재하는 시점에 observe 연결
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerObserve()
    }

    override fun onStart() {
        super.onStart()
        // 다이얼로그 크기(원하면 조절)
        dialog?.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    // -------------------------
    // 옷 클릭 시 결과 전달 + 닫기
    // -------------------------
    private fun setupPickListener() {
        clothAdapter.onItemClickListener = { item ->
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply {
                    putInt(KEY_CLOTHES_ID, item.clothesId)
                    putString(KEY_PHOTO_URL, item.photoUrl)
                }
            )
            dismiss()
        }
    }

    // 리스트 초기화
    private fun initRecyclerViews() {
        setupPickListener()

        binding.glCloset.apply {
            adapter = clothAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
            setHasFixedSize(true)
        }
    }

    // 검색 다이얼로그(ClosetFragment 로직 재사용)
    private fun searchDialog() {
        binding.ibtnSearchFilter.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_search_filter, null, false)

            val tagsSection = dialogView.findViewById<View>(R.id.section_tags)
            val seasonSection = dialogView.findViewById<View>(R.id.section_season)
            val colorSection = dialogView.findViewById<View>(R.id.section_color)
            val btnApply = dialogView.findViewById<View>(R.id.btn_search_filter)

            TagOptions.render(tagsSection, requireContext())
            SeasonOptions.render(seasonSection, requireContext())
            colorAdapter = ColorOptions.setup(colorSection)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            dialog.show()

            btnApply.setOnClickListener {
                currentTags = TagOptions.getSelectedTag(tagsSection)
                currentSeasons = SeasonOptions.getSelectedSeason(seasonSection)
                currentColor = colorAdapter.getSelectedColor()

                runSearch()
                dialog.dismiss()
            }
        }
    }

    // 스위치 체크 여부 확인
    private fun checkSwitch() {
        checkedOnlyMyCloth = binding.swOnlyMyCloth.isChecked

        binding.swOnlyMyCloth.setOnCheckedChangeListener { _, isChecked ->
            checkedOnlyMyCloth = isChecked
            runSearch()
        }
    }

    // 검색 실행
    private fun runSearch() {
        viewModel.getClothesList(
            currentTags,
            currentColor,
            currentSeasons,
            checkedOnlyMyCloth
        )
    }

    private fun registerObserve() {
        viewModel.closetData.observe(viewLifecycleOwner, closetObserver)
    }

    // View가 사라질 때 observe 해제 (DialogFragment 재사용/회전 시 안전)
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.closetData.removeObserver(closetObserver) // ✅ 추가
        _binding = null
    }

    // 탭 선택에 맞게 화면에 보여줄 리스트 갱신
    private fun selectedTab() {
        binding.tabCloset.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                applyTabItems(viewModel.closetData.value)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // 탭 대상 적용하기
    private fun applyTabItems(data: com.ssafy.closetory.dto.ClosetResponse?) {
        if (data == null) return

        val position = binding.tabCloset.selectedTabPosition
        val list = when (position) {
            0 -> data.topClothes ?: emptyList()
            1 -> data.bottomClothes ?: emptyList()
            2 -> data.outerClothes ?: emptyList()
            3 -> data.shoes ?: emptyList()
            4 -> data.bags ?: emptyList()
            5 -> data.accessories ?: emptyList()
            else -> emptyList()
        }

        clothAdapter.submitList(list)
    }

    companion object {
        const val REQUEST_KEY = "clothes_pick_request"
        const val KEY_CLOTHES_ID = "clothes_id"
        const val KEY_PHOTO_URL = "photo_url"
    }
}
