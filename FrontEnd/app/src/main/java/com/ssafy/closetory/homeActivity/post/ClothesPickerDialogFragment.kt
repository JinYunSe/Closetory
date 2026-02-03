package com.ssafy.closetory.homeActivity.post

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.ssafy.closetory.R
import com.ssafy.closetory.databinding.FragmentClosetBinding
import com.ssafy.closetory.dto.ClosetResponse
import com.ssafy.closetory.homeActivity.adapter.ClothesAdapter
import com.ssafy.closetory.homeActivity.closet.ClosetViewModel
import com.ssafy.closetory.util.ColorOptions
import com.ssafy.closetory.util.SeasonOptions
import com.ssafy.closetory.util.TagOptions
import kotlinx.coroutines.launch

private const val TAG = "ClothesPickerDialogFrag_싸피"

class ClothesPickerDialogFragment : DialogFragment() {

    private var _binding: FragmentClosetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ClosetViewModel by viewModels()
    private val clothAdapter = ClothesAdapter()

    // ClosetFragment에서 쓰던 상태값들 그대로
    private lateinit var colorAdapter: ColorOptions.ColorAdapter
    private var currentTags: List<Int> = TagOptions.items.map { it.code }
    private var currentSeasons: List<Int> = SeasonOptions.items.map { it.code }
    private var currentColor: String? = null
    private var checkedOnlyMyCloth: Boolean = false

    private val closetObserver = Observer<ClosetResponse?> { data ->
        if (data == null) return@Observer
        Log.d(TAG, "closetObserver data=$data")
        applyTabItems(data)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentClosetBinding.inflate(inflater, container, false)
        return binding.root
    }

    // observe, 최초검색 처리
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 게시글에는 내 옷만 보이게 처리
        checkedOnlyMyCloth = true
        binding.tvOnlyMyCloth.visibility = View.GONE
        binding.swOnlyMyCloth.visibility = View.GONE

        initRecyclerViews()
        searchDialog()
        selectedTab()
        registerObserve()

        // 최초 조회
        runSearch()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    // 옷 클릭 시 결과 전달 + 다이얼로그 닫기
    private fun setupPickListener() {
        clothAdapter.onItemClick = { item ->
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

            val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
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

    // 검색 실행
    private fun runSearch() {
        Log.d(TAG, "runSearch tags=$currentTags seasons=$currentSeasons color=$currentColor onlyMy=$checkedOnlyMyCloth")
        viewModel.getClothesList(
            currentTags,
            currentColor,
            currentSeasons,
            checkedOnlyMyCloth
        )
    }

    private fun registerObserve() {
        viewModel.closetData.observe(viewLifecycleOwner, closetObserver)

        // 에러/메시지 다이얼로그에서 토스트로 확인
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.message.collect { msg ->
                if (!msg.isNullOrBlank()) {
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
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
    private fun applyTabItems(data: ClosetResponse?) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        // observe 제거도 필드 observer로 정확히 제거
        viewModel.closetData.removeObserver(closetObserver)
        _binding = null
    }

    companion object {
        const val REQUEST_KEY = "clothes_pick_request"
        const val KEY_CLOTHES_ID = "clothes_id"
        const val KEY_PHOTO_URL = "photo_url"
    }
}
