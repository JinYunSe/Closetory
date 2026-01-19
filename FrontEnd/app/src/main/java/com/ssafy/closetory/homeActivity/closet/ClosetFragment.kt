package com.ssafy.closetory.homeActivity.closet

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentClosetBinding
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.homeActivity.adpter.ClothAdapter
import com.ssafy.closetory.util.ClothOptions
import com.ssafy.closetory.util.ClothTypeOptions
import com.ssafy.closetory.util.SeasonOptions
import com.ssafy.closetory.util.TagOptions

class ClosetFragment : BaseFragment<FragmentClosetBinding>(FragmentClosetBinding::bind, R.layout.fragment_closet) {

    private lateinit var homeActivity: HomeActivity

    private lateinit var colorAdapter: ClothOptions.ColorAdapter

    @SuppressLint("CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireContext() as HomeActivity

        // 리사이클러 뷰에 Adapter 붙이기
        binding.lvTopCloth.adapter = ClothAdapter()
        binding.lvBottomCloth.adapter = ClothAdapter()
        binding.lvOuter.adapter = ClothAdapter()
        binding.lvOnePiece.adapter = ClothAdapter()
        binding.lvShoes.adapter = ClothAdapter()
        binding.lvHat.adapter = ClothAdapter()
        binding.lvAccessory.adapter = ClothAdapter()

        binding.swFavorites.isChecked
        binding.swOnlyMyCloth.isChecked

        binding.btnSearchDialog.setOnClickListener {
            val dialogView = LayoutInflater.from(homeActivity)
                .inflate(R.layout.dialog_search_filter, null, false)

            // 다른 XML 레이어 파일 가져오기
            val tagsSection = dialogView.findViewById<View>(R.id.section_tags)
            val seasonSection = dialogView.findViewById<View>(R.id.section_season)
            val clothTypeSection = dialogView.findViewById<View>(R.id.section_cloth_type)
            val colorSection = dialogView.findViewById<View>(R.id.section_color)

            // 다이얼로그 내부의 ChipGroup 설정을 위해 가져옴
            val tagsChipGroup = tagsSection.findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroup)

            // 다이얼로그 내부의 ChipGroup 설정을 위해 가져옴
            val seasonChipGroup = seasonSection.findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroup)

            // 다이얼로그 내부의 ChipGroup 설정을 위해 가져옴
            val clothTypeChipGroup = clothTypeSection.findViewById<com.google.android.material.chip.ChipGroup>(
                R.id.chipGroup
            )

            // 선택이 필수가 아니게 지정
            tagsChipGroup.isSelectionRequired = false
            seasonChipGroup.isSelectionRequired = false
            clothTypeChipGroup.isSingleSelection = false

            val btnSearchFilter = dialogView.findViewById<View>(R.id.btn_search_filter)

            TagOptions.render(tagsSection, homeActivity)
            SeasonOptions.render(seasonSection, homeActivity)
            ClothTypeOptions.render(clothTypeSection, homeActivity)
            colorAdapter = ClothOptions.setup(colorSection)

            val dialog = AlertDialog.Builder(homeActivity)
                .setView(dialogView)
                .create()

            dialog.show()

            btnSearchFilter.setOnClickListener {
            }
        }
    }
}
