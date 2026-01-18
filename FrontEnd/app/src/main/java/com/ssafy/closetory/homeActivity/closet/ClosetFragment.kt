package com.ssafy.closetory.homeActivity.closet

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentClosetBinding
import com.ssafy.closetory.dto.ClothItemDto
import com.ssafy.closetory.homeActivity.HomeActivity

class ClosetFragment :
    BaseFragment<FragmentClosetBinding>(FragmentClosetBinding::bind, R.layout.fragment_closet) {

    private lateinit var homeActivity: HomeActivity

    private val topAdapter = ClothAdapter()
    private val bottomAdapter = ClothAdapter()
    private val outerAdapter = ClothAdapter()
    private val onePieceAdapter = ClothAdapter()
    private val shoesAdapter = ClothAdapter()
    private val hatAdapter = ClothAdapter()
    private val accessoryAdapter = ClothAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireContext() as HomeActivity

        // LayoutManagers
        binding.lvTopCloth.layoutManager =
            LinearLayoutManager(homeActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.lvBottomCloth.layoutManager =
            LinearLayoutManager(homeActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.lvOuter.layoutManager =
            LinearLayoutManager(homeActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.lvOnePiece.layoutManager =
            LinearLayoutManager(homeActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.lvShoes.layoutManager =
            LinearLayoutManager(homeActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.lvHat.layoutManager =
            LinearLayoutManager(homeActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.lvAccessory.layoutManager =
            LinearLayoutManager(homeActivity, LinearLayoutManager.HORIZONTAL, false)

        // Adapters
        binding.lvTopCloth.adapter = topAdapter
        binding.lvBottomCloth.adapter = bottomAdapter
        binding.lvOuter.adapter = outerAdapter
        binding.lvOnePiece.adapter = onePieceAdapter
        binding.lvShoes.adapter = shoesAdapter
        binding.lvHat.adapter = hatAdapter
        binding.lvAccessory.adapter = accessoryAdapter

        // 더미 데이터 주입 (일단 화면 확인용)
        val dummy = listOf(
            ClothItemDto(clothId = 1, clothImage = "https://picsum.photos/200/200?1"),
            ClothItemDto(clothId = 2, clothImage = "https://picsum.photos/200/200?2"),
            ClothItemDto(clothId = 3, clothImage = "https://picsum.photos/200/200?3"),
            ClothItemDto(clothId = 4, clothImage = "https://picsum.photos/200/200?4"),
        )

        topAdapter.submitList(dummy)
        bottomAdapter.submitList(dummy)
        outerAdapter.submitList(dummy)
        onePieceAdapter.submitList(dummy)
        shoesAdapter.submitList(dummy)
        hatAdapter.submitList(dummy)
        accessoryAdapter.submitList(dummy)
    }
}
