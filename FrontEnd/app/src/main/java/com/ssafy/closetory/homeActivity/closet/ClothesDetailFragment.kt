package com.ssafy.closetory.homeActivity.closet

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentClothesDetailBinding
import com.ssafy.closetory.dto.ClothesItemDto
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.homeActivity.adapter.RecommendClothesAdapter
import com.ssafy.closetory.util.ChipUtils
import com.ssafy.closetory.util.ClothTypeOptions
import com.ssafy.closetory.util.ColorOptions
import com.ssafy.closetory.util.SeasonOptions
import com.ssafy.closetory.util.TagOptions
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

    // 추천 목록 어댑터
    private val recommendAdapter = RecommendClothesAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireContext() as HomeActivity

        homeActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.navigation_closet, false)
        }

        binding.rvRecommend.apply {
            layoutManager = LinearLayoutManager(homeActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendAdapter
            setHasFixedSize(true)
        }

        registerObserve()
        setupPickListener()

        Log.d(TAG, "옷 상세 정보 조회 clothesId: $clothesId")
        closetViewModel.getClothesDetail(clothesId)
        closetViewModel.getRecommendedClothes(clothesId)

        binding.ibtnBookmark.setOnClickListener {
            // isRental이 true이면 대여, false이면 대여 취소
            if (isRental) {
                // 대여 취소 요청
                closetViewModel.deleteClothesRental(clothesId)
            } else {
                // 다시 대여 요청
                closetViewModel.postClothesRental(clothesId)
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
            binding.tvSeasons.text = formatSeasons(item.seasons)
            binding.tvColor.text = ColorOptions.englishToKorean(item.color)

            // tags chip
            binding.cgTags.removeAllViews()
            val tags = item.tags.orEmpty()
            if (tags.isEmpty()) {
                binding.cgTags.visibility = View.GONE
            } else {
                binding.cgTags.visibility = View.VISIBLE
                tags.forEach { tag ->
                    val chip = ChipUtils.createChoiceChip(
                        context = homeActivity,
                        text = "#$tag",
                        checkable = true,
                        checked = true,
                        clickable = false,
                        focusable = false
                    )
                    binding.cgTags.addView(chip)
                }
            }

            // 본인 옷이면 수정/삭제 보이기, 남의 옷이면 북마크만
            if (item.isMine == true) {
                binding.editLinearLayout.visibility = View.VISIBLE
                binding.ibtnEdit.visibility = View.VISIBLE
                binding.tvRecommendTitle.visibility = View.VISIBLE
            } else {
                isRental = true
                binding.editLinearLayout.visibility = View.GONE
                binding.tvRecommendTitle.visibility = View.GONE
                binding.rvRecommend.visibility = View.GONE
                binding.tvRecommendEmpty.visibility = View.GONE
                binding.ibtnBookmark.setImageResource(R.drawable.baseline_bookmark_24)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            closetViewModel.deleteSuccess.collect { success ->
                if (success != true) return@collect

                val navController = findNavController()

                // "새로고침" 신호 전달 (옷장 화면이 백스택에 있을 때만)
                runCatching {
                    navController.getBackStackEntry(R.id.navigation_closet)
                        .savedStateHandle
                        .set("refreshCloset", true)
                }

                // 옷장 화면이 없으면 직접 이동
                val popped = navController.popBackStack(R.id.navigation_closet, false)
                if (!popped) {
                    navController.navigate(
                        R.id.navigation_closet,
                        null,
                        navOptions {
                            popUpTo(navController.graph.startDestinationId) { inclusive = false }
                            launchSingleTop = true
                        }
                    )
                }
            }
        }

        closetViewModel.recommendedClothes.observe(viewLifecycleOwner) { list ->
            recommendAdapter.submitList(list)
            val isEmpty = list.isNullOrEmpty()
            binding.rvRecommend.visibility = View.VISIBLE
            binding.tvRecommendEmpty.visibility =
                if (isEmpty && currentItem?.isMine == true) View.VISIBLE else View.GONE
        }

        viewLifecycleOwner.lifecycleScope.launch {
            closetViewModel.clothesRental.collect { check ->
                isRental = check
                if (check) {
                    binding.ibtnBookmark.setImageResource(R.drawable.baseline_bookmark_24)
                } else {
                    binding.ibtnBookmark.setImageResource(R.drawable.baseline_bookmark_border_24)
                }
            }
        }
    }

    private fun setupPickListener() {
        recommendAdapter.onItemClickListener = { item ->
            findNavController().navigate(
                R.id.action_navigation_clothes_detail_self,
                Bundle().apply { putInt("clothesId", item.clothesId) }
            )
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

    private fun formatSeasons(seasons: List<String>?): String {
        if (seasons.isNullOrEmpty()) return ""

        val codeToLabel = mapOf(
            1 to "봄",
            2 to "여름",
            3 to "가을",
            4 to "겨울"
        )

        return seasons
            .mapNotNull { SeasonOptions.toCode(it) }
            .distinct()
            .sorted()
            .mapNotNull { codeToLabel[it] }
            .joinToString(" · ")
    }
}

