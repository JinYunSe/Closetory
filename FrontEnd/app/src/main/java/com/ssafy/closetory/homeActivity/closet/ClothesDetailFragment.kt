package com.ssafy.closetory.homeActivity.closet

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentClothesDetailBinding
import com.ssafy.closetory.dto.ClothesItemDto
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.util.ClothTypeOptions
import com.ssafy.closetory.util.ColorOptions
import com.ssafy.closetory.util.SeasonOptions
import com.ssafy.closetory.util.TagOptions
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val TAG = "ClothesDetailFragment_싸피"

class ClothesDetailFragment :
    BaseFragment<FragmentClothesDetailBinding>(
        FragmentClothesDetailBinding::bind,
        R.layout.fragment_clothes_detail
    ) {

    private val clothesId: Int by lazy {
        requireArguments().getInt("clothesId", -1)
    }

    private val closetViewModel: ClosetViewModel by viewModels()
    private lateinit var homeActivity: HomeActivity

    private var isRental = false

    // 수정 화면으로 넘길 “현재 상세 아이템” 보관
    private var currentItem: ClothesItemDto? = null // 네 프로젝트 모델명에 맞춰라

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireContext() as HomeActivity

        homeActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.navigation_closet, false)
        }

        binding.rvRecommend.layoutManager =
            LinearLayoutManager(homeActivity, LinearLayoutManager.HORIZONTAL, false)

        registerObserve()

        Log.d(TAG, "옷 상세 정보 조회 clothesId: $clothesId")
        closetViewModel.getClothesDetail(clothesId)

        binding.ibtnBookmark.setOnClickListener {
            if (isRental) {
                isRental = false
                binding.ibtnBookmark.setImageResource(R.drawable.baseline_bookmark_border_24)
            } else {
                isRental = true
                binding.ibtnBookmark.setImageResource(R.drawable.baseline_bookmark_24)
            }
        }

        binding.ibtnEdit.setOnClickListener {
            MaterialAlertDialogBuilder(homeActivity)
                .setTitle("옷 정보 수정")
                .setMessage("옷의 정보를 수정하시겠습니까?")
                .setPositiveButton("취소", null)
                .setNegativeButton("수정") { _, _ ->
                    val item = currentItem ?: return@setNegativeButton
                    navigateToEdit(item)
                }
                .show()
        }

        binding.ibtnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(homeActivity)
                .setTitle("옷 삭제")
                .setMessage("옷을 삭제하시겠습니까?\n삭제 후에는 복구할 수 없습니다.")
                .setPositiveButton("취소", null)
                .setNegativeButton("삭제") { _, _ ->
                    closetViewModel.deleteClothes(clothesId)
                }
                .show()
        }
    }

    private fun registerObserve() {
        closetViewModel.clothesData.observe(viewLifecycleOwner) { item ->
            if (item == null) return@observe

            // 수정 이동을 위해 보관
            currentItem = item

            Log.d(TAG, "옷 상세 정보 조회 통신 결과 clothesId : ${item.clothesId}")

            Glide.with(binding.ivPhoto)
                .load(item.photoUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(binding.ivPhoto)

            binding.tvType.text = ClothTypeOptions.englishToKorean(item.clothesType)
            binding.tvSeasons.text = item.seasons?.joinToString(" · ")
            binding.tvColor.text = ColorOptions.englishToKorean(item.color)

            // tags chip
            binding.cgTags.removeAllViews()
            val tags = item.tags.orEmpty()
            if (tags.isEmpty()) {
                binding.cgTags.visibility = View.GONE
            } else {
                binding.cgTags.visibility = View.VISIBLE
                tags.forEach { tag ->
                    val chip = com.google.android.material.chip.Chip(homeActivity).apply {
                        text = "#$tag"
                        isCheckable = true
                        isChecked = true
                        isClickable = false
                        isFocusable = false
                        setEnsureMinTouchTargetSize(false)
                        setChipDrawable(
                            com.google.android.material.chip.ChipDrawable.createFromAttributes(
                                homeActivity,
                                null,
                                0,
                                com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice
                            )
                        )
                        chipBackgroundColor = homeActivity.getColorStateList(R.color.chip_bg_selector)
                        setTextColor(homeActivity.getColorStateList(R.color.chip_text_selector))
                    }
                    binding.cgTags.addView(chip)
                }
            }

            // 본인 옷이면 수정/삭제 보이기, 남의 옷이면 북마크만
            if (item.isMine == true) {
                binding.editLinearLayout.visibility = View.VISIBLE
                binding.ibtnEdit.visibility = View.VISIBLE
            } else {
                isRental = true
                binding.editLinearLayout.visibility = View.GONE
                binding.ibtnBookmark.setImageResource(R.drawable.baseline_bookmark_24)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            closetViewModel.deleteSuccess.collect { success ->
                if (success != true) return@collect

                // "새로고침"신호 제공 => 이걸로 옷 장 목록 조회가 다시 이뤄져야 한다고 알림
                findNavController().previousBackStackEntry?.savedStateHandle?.set("refreshCloset", true)

                // 뒤로가기
                findNavController().popBackStack(R.id.navigation_closet, false)
            }
        }
    }

    private fun navigateToEdit(item: ClothesItemDto) {
        val tagsInt = ArrayList(item.tags.orEmpty().mapNotNull { TagOptions.toCode(it) })
        val seasonsInt = ArrayList(item.seasons.orEmpty().map { SeasonOptions.toCode(it) })

        val bundle = Bundle().apply {
            putString("mode", "edit")
            putInt("clothesId", item.clothesId)
            putString("photoUrl", item.photoUrl)

            putIntegerArrayList("tags", tagsInt)
            putString("clothesType", item.clothesType)
            putIntegerArrayList("seasons", seasonsInt)
            putString("color", item.color)
        }

        findNavController().navigate(
            R.id.action_clothes_detail_to_registration,
            bundle
        )
    }
}
