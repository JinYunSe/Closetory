package com.ssafy.closetory.homeActivity.closet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentClosetBinding
import com.ssafy.closetory.dto.ClosetResponse
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.homeActivity.adpter.ClothAdapter
import com.ssafy.closetory.util.ColorOptions
import com.ssafy.closetory.util.SeasonOptions
import com.ssafy.closetory.util.TagOptions

private const val TAG = "ClosetFragment_싸피"
class ClosetFragment : BaseFragment<FragmentClosetBinding>(FragmentClosetBinding::bind, R.layout.fragment_closet) {

    private val viewModel: ClosetViewModel by viewModels()

    private lateinit var homeActivity: HomeActivity

    private lateinit var colorAdapter: ColorOptions.ColorAdapter

    // 현재 선택된 필터 변수에 담기
    private var currentTags: List<Int> = TagOptions.items.map { it.code }
    private var currentSeasons: List<Int> = SeasonOptions.items.map { it.code }
    private var currentColor: String? = null
    private var checkedOnlyMyCloth: Boolean = false

    // 옷 어댑터
    private val clothAdapter = ClothAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireContext() as HomeActivity

        initRecyclerViews()
        checkSwitch()
        searchDialog()
        registerObserve()
        selectedTab()

        // 옷 검색
        runSearch()
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

            // 태그, 계절, 색상 항목들 가져오기
            TagOptions.render(tagsSection, homeActivity)
            SeasonOptions.render(seasonSection, homeActivity)
            colorAdapter = ColorOptions.setup(colorSection)

            val dialog = AlertDialog.Builder(homeActivity)
                .setView(dialogView)
                .create()

            dialog.show()

            btnApply.setOnClickListener {
                var selectedTags = TagOptions.getSelectedTag(tagsSection)
                var selectedSeasons = SeasonOptions.getSelectedSeason(seasonSection)
                val selectedColor = colorAdapter.getSelectedColor()

                // 필터를 변수에 담아 두기
                currentTags = selectedTags
                currentSeasons = selectedSeasons
                currentColor = selectedColor

                runSearch()
                dialog.dismiss()
            }
        }
    }

    // 스위치 체크 여부 확인
    fun checkSwitch() {
        checkedOnlyMyCloth = binding.swOnlyMyCloth.isChecked

        binding.swOnlyMyCloth.setOnCheckedChangeListener { _, isChecked ->
            checkedOnlyMyCloth = isChecked
            runSearch()
        }
    }

    // 검색 실행
    fun runSearch() {
        Log.d(
            TAG,
            "runSearch tags=$currentTags seasons=$currentSeasons color=$currentColor onlyMy=$checkedOnlyMyCloth"
        )

        viewModel.getClothesList(
            currentTags,
            currentColor,
            currentSeasons,
            checkedOnlyMyCloth
        )
    }

    // 리스트 초기화
    fun initRecyclerViews() {
        // 리사이클러 뷰에 Adapter 붙이기
        binding.glCloset.apply {
            adapter = clothAdapter
            layoutManager = GridLayoutManager(homeActivity, 3)
            setHasFixedSize(true)
        }
    }

    fun registerObserve() {
        // 서버 통신 결과 리스트 반영하기
        viewModel.closetData.observe(viewLifecycleOwner) { data: ClosetResponse? ->
            if (data == null) return@observe

            Log.d(TAG, "registerObserve Data : $data")

            // 현재 선택된 탭 기준 요소들 집어 넣기
            applyTabItems(data)
        }

        // 에러 발생의 경우 토스트 메시지 띄우기
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message == null) return@observe
            showToast(message)
        }
    }

    // 탭 선택에 맞게 화면에 보여줄 리스트 갱신
    fun selectedTab() {
        // 리스너 구현체를 만들어 TabLayout에 외부에서 넣기
        binding.tabCloset.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            // 새로운 탭이 호출 될 경우
            override fun onTabSelected(tab: TabLayout.Tab) {
                applyTabItems(viewModel.closetData.value)
            }

            // 아래는 어쩔 수 없이 override 해야 하는 메서드
            // 기존에 선택된 대상이 해제된 순간
            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            // 똑같은 대상을 눌렀을 때
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }
        })
    }

    // 댑 대상 적용하기
    fun applyTabItems(data: ClosetResponse?) {
        if (data == null) return

        // 누른 대상 index 가져오기
        val position = binding.tabCloset.selectedTabPosition

        // ?: emptyList()은 요청한 옷 목록에 요소가 없을 경우 빈 list로 보여주기 위해 사용
        val list = when (position) {
            0 -> data.topClothes ?: emptyList()
            1 -> data.bottomClothes ?: emptyList()
            2 -> data.outerClothes ?: emptyList()
            3 -> data.shoes ?: emptyList()
            4 -> data.bags ?: emptyList()
            5 -> data.accessories ?: emptyList()
            else -> emptyList()
        }

        // 리스트 갱신을 알리기
        clothAdapter.submitList(list)
    }
}
