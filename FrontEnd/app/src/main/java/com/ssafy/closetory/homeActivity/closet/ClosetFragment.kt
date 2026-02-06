package com.ssafy.closetory.homeActivity.closet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentClosetBinding
import com.ssafy.closetory.dto.ClosetResponse
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.homeActivity.adapter.ClothesAdapter
import com.ssafy.closetory.util.ColorOptions
import com.ssafy.closetory.util.SeasonOptions
import com.ssafy.closetory.util.TagOptions
import kotlinx.coroutines.launch

private const val TAG = "ClosetFragment_싸피"

class ClosetFragment : BaseFragment<FragmentClosetBinding>(FragmentClosetBinding::bind, R.layout.fragment_closet) {

    private val viewModel: ClosetViewModel by viewModels()
    private lateinit var homeActivity: HomeActivity
    private lateinit var colorAdapter: ColorOptions.ColorAdapter

    /**
     * null  = 필터 미적용(전체)
     * list  = 선택된 필터 적용
     */
    private var currentTags: List<Int>? = null
    private var currentSeasons: List<Int>? = null
    private var currentColor: String? = null
    private var checkedOnlyMyCloth: Boolean = false

    private val clothAdapter = ClothesAdapter()

    private var suppressSwitchListener = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireContext() as HomeActivity

        initRecyclerViews()
        bindAdapterClicks()
        restoreFilterState()
        checkSwitch()
        searchDialog()
        registerObserve()
        selectedTab()
        observeRefreshSignal()

        runSearch()
    }

    private fun bindAdapterClicks() {
        clothAdapter.onItemClick = { item ->
            findNavController().navigate(
                R.id.action_closet_to_clothes_detail,
                bundleOf("clothesId" to item.clothesId)
            )
        }

        // 북마크 클릭이 필요하면 여기 연결
        // clothAdapter.onBookmarkClick = { item -> ... }
    }

    // 검색 다이얼로그
    fun searchDialog() {
        binding.ibtnSearchFilter.setOnClickListener {
            val dialogView = LayoutInflater.from(homeActivity)
                .inflate(R.layout.dialog_search_filter, null, false)

            val tagsSection = dialogView.findViewById<View>(R.id.section_tags)
            val seasonSection = dialogView.findViewById<View>(R.id.section_season)
            val colorSection = dialogView.findViewById<View>(R.id.section_color)
            val btnApply = dialogView.findViewById<View>(R.id.btn_search_filter)

            // 렌더
            TagOptions.render(tagsSection, homeActivity)
            SeasonOptions.render(seasonSection, homeActivity)
            colorAdapter = ColorOptions.setup(colorSection)

            // 기존 선택값 복원 (null이면 전체라서 복원할 게 없음)
            currentTags?.let { TagOptions.setSelectedTag(tagsSection, it) }
            currentSeasons?.let { SeasonOptions.setSelectedSeason(seasonSection, it) }
            currentColor?.let { colorAdapter.setSelectedColor(it) }

            val dialog = AlertDialog.Builder(homeActivity)
                .setView(dialogView)
                .create()

            dialog.show()

            btnApply.setOnClickListener {
                val selectedTags = TagOptions.getSelectedTag(tagsSection)
                val selectedSeasons = SeasonOptions.getSelectedSeason(seasonSection)
                val selectedColor = colorAdapter.getSelectedColor()

                // "아무것도 선택 안 함"은 전체로 처리(null)
                currentTags = selectedTags.takeIf { it.isNotEmpty() }
                currentSeasons = selectedSeasons.takeIf { it.isNotEmpty() }
                currentColor = selectedColor
                persistFilterState()

                runSearch()
                dialog.dismiss()
            }
        }
    }

    // 스위치 체크 여부 확인
    fun checkSwitch() {
        suppressSwitchListener = true
        binding.swOnlyMyCloth.isChecked = checkedOnlyMyCloth
        suppressSwitchListener = false

        binding.swOnlyMyCloth.setOnCheckedChangeListener { _, isChecked ->
            if (suppressSwitchListener) return@setOnCheckedChangeListener
            checkedOnlyMyCloth = isChecked
            persistFilterState()
            runSearch()
        }
    }

    // 검색 실행
    fun runSearch() {
        Log.d(
            TAG,
            "runSearch tags=$currentTags seasons=$currentSeasons color=$currentColor onlyMy=$checkedOnlyMyCloth"
        )

        persistFilterState()
        viewModel.getClothesList(
            tags = currentTags,
            color = currentColor,
            seasons = currentSeasons,
            onlyMine = checkedOnlyMyCloth
        )
    }

    // 리스트 초기화
    fun initRecyclerViews() {
        binding.glCloset.apply {
            adapter = clothAdapter
            layoutManager = GridLayoutManager(homeActivity, 3)
            setHasFixedSize(true)
        }
    }

    fun registerObserve() {
        viewModel.closetData.observe(viewLifecycleOwner) { data: ClosetResponse? ->
            if (data == null) return@observe
            Log.d(TAG, "registerObserve Data : $data")
            applyTabItems(data)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.message.collect { message ->
                if (message.isNullOrBlank()) return@collect
                showToast(message)
            }
        }
    }

    fun selectedTab() {
        binding.tabCloset.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                applyTabItems(viewModel.closetData.value)
            }
            override fun onTabUnselected(p0: TabLayout.Tab?) {}
            override fun onTabReselected(p0: TabLayout.Tab?) {}
        })
    }

    fun applyTabItems(data: ClosetResponse?) {
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

        binding.tvTotalCount.text = list.size.toString()
        binding.glCloset.visibility = View.VISIBLE
        binding.tvEmptyCloset.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun observeRefreshSignal() {
        val handle = findNavController().currentBackStackEntry?.savedStateHandle ?: return

        handle.getLiveData<Boolean>("refreshCloset").observe(viewLifecycleOwner) { need ->
            if (need) {
                handle.remove<Boolean>("refreshCloset")
                runSearch()
            }
        }
    }

    private fun persistFilterState() {
        val handle = findNavController().currentBackStackEntry?.savedStateHandle ?: return
        handle["CLOSET_FILTER_TAGS"] = currentTags?.let { ArrayList(it) }
        handle["CLOSET_FILTER_SEASONS"] = currentSeasons?.let { ArrayList(it) }
        handle["CLOSET_FILTER_COLOR"] = currentColor
        handle["CLOSET_FILTER_ONLY_MINE"] = checkedOnlyMyCloth
    }

    private fun restoreFilterState() {
        val handle = findNavController().currentBackStackEntry?.savedStateHandle ?: return
        currentTags = handle.get<ArrayList<Int>>("CLOSET_FILTER_TAGS")?.toList()
        currentSeasons = handle.get<ArrayList<Int>>("CLOSET_FILTER_SEASONS")?.toList()
        currentColor = handle.get<String>("CLOSET_FILTER_COLOR")
        checkedOnlyMyCloth = handle.get<Boolean>("CLOSET_FILTER_ONLY_MINE") ?: checkedOnlyMyCloth
    }
}
