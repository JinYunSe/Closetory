package com.ssafy.closetory.homeActivity.addClose

import android.os.Bundle
import android.view.View
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentAddClothBinding
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.util.ClothTypeOptions
import com.ssafy.closetory.util.ColorOptions
import com.ssafy.closetory.util.SeasonOptions
import com.ssafy.closetory.util.TagOptions

class AddClothFragment :
    BaseFragment<FragmentAddClothBinding>(FragmentAddClothBinding::bind, R.layout.fragment_add_cloth) {

    private lateinit var homeActivity: HomeActivity

    private lateinit var colorAdapter: ColorOptions.ColorAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireContext() as HomeActivity

        // 다른 XML 레이어 파일 가져오기
        val tagsSection = view.findViewById<View>(R.id.section_tags)
        val seasonSection = view.findViewById<View>(R.id.section_season)
        val clothTypeSection = view.findViewById<View>(R.id.section_cloth_type)
        val colorSection = view.findViewById<View>(R.id.section_color)

        TagOptions.render(tagsSection, homeActivity)
        SeasonOptions.render(seasonSection, homeActivity)
        ClothTypeOptions.render(clothTypeSection, homeActivity)
        colorAdapter = ColorOptions.setup(colorSection)
    }
}
