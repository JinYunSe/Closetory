package com.ssafy.closetory.homeActivity.closet

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentClothesDetailBinding
import com.ssafy.closetory.homeActivity.HomeActivity

private const val TAG = "ClothesDetailFragment_싸피"
class ClothesDetailFragment :
    BaseFragment<FragmentClothesDetailBinding>(FragmentClothesDetailBinding::bind, R.layout.fragment_clothes_detail) {

    private val clothesId: Int by lazy {
        requireArguments().getInt("clothesId")
    }

    private val viewModel: ClosetViewModel by viewModels()

    private lateinit var homeActivity: HomeActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireContext() as HomeActivity

        Log.d(TAG, "옷 상세 정보 조회 clothesId: $clothesId")
        viewModel.getClothesDetail(clothesId)

        registerObserve()
    }

    fun registerObserve() {
        viewModel.clothesData.observe(viewLifecycleOwner) {
//            binding.ivPhoto.load(it.photoUrl)
        }
    }
}
