package com.ssafy.closetory.homeActivity.closet

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentClosetBinding
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
    private var currentTags: List<String> = TagOptions.items.map { it.codeEnglish }
    private var currentSeasons: List<String> = SeasonOptions.items.map { it.codeEnglish }
    private var currentColor: String? = null
    private var checkedFavorites: Boolean = false
    private var checkedOnlyMyCloth: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireContext() as HomeActivity

        initListViews()
        initSwitch()
        initSearchDialog()

        registerObserve()
    }

    // 검색 다이얼로그
    fun initSearchDialog() {
        binding.btnSearchDialog.setOnClickListener {
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

    // 스위치 초기화
    fun initSwitch() {
        checkedFavorites = binding.swFavorites.isChecked
        checkedOnlyMyCloth = binding.swOnlyMyCloth.isChecked

        binding.swFavorites.setOnCheckedChangeListener { _, isChecked ->
            checkedFavorites = isChecked
            runSearch()
        }

        binding.swOnlyMyCloth.setOnCheckedChangeListener { _, isChecked ->
            checkedOnlyMyCloth = isChecked
            runSearch()
        }
    }

    // 검색 실행
    fun runSearch() {
        Log.d(
            TAG,
            "runSearch tags=$currentTags seasons=$currentSeasons color=$currentColor fav=$checkedFavorites onlyMy=$checkedOnlyMyCloth"
        )

        viewModel.getClothesList(
            currentTags,
            currentColor,
            currentSeasons,
            checkedFavorites,
            checkedOnlyMyCloth
        )
    }

    // 리스트 초기화
    fun initListViews() {
        // 리사이클러 뷰에 Adapter 붙이기
        binding.lvTopCloth.adapter = ClothAdapter()
        binding.lvBottomCloth.adapter = ClothAdapter()
        binding.lvOuter.adapter = ClothAdapter()
        binding.lvShoes.adapter = ClothAdapter()
        binding.lvHat.adapter = ClothAdapter()
        binding.lvAccessory.adapter = ClothAdapter()
    }

    fun registerObserve() {
        viewModel.closetData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d(TAG, "registerObserve Data : $data")
            }
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) Log.d(TAG, "registerObserve Message : $msg")
        }
    }
}
