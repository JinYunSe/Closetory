package com.ssafy.closetory.homeActivity.closet

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentClothesDetailBinding
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.util.ClothTypeOptions
import com.ssafy.closetory.util.ColorOptions

private const val TAG = "ClothesDetailFragment_싸피"
class ClothesDetailFragment :
    BaseFragment<FragmentClothesDetailBinding>(FragmentClothesDetailBinding::bind, R.layout.fragment_clothes_detail) {

    private val clothesId: Int by lazy {
        requireArguments().getInt("clothesId")
    }

    private val viewModel: ClosetViewModel by viewModels()

    private lateinit var homeActivity: HomeActivity

    private var isRental = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireContext() as HomeActivity

        Log.d(TAG, "옷 상세 정보 조회 clothesId: $clothesId")
        viewModel.getClothesDetail(clothesId)

        binding.rvRecommend.layoutManager = LinearLayoutManager(homeActivity, LinearLayoutManager.HORIZONTAL, false)

        registerObserve()

        // 본인 옷인 경우는  View.Gone이라 UI에서 조작 불가능
        // 가져온 옷만 조작 가능
        binding.ibtnBookmark.setOnClickListener {
            // isRental이 true에서 클릭하면 가져오기 취소가 된다.
            if (isRental) {
                isRental = false
                binding.ibtnBookmark.setImageResource(R.drawable.baseline_bookmark_border_24)
            } else {
                // 다시 가져오기 상황으로 설정한 경우
                isRental = true
                binding.ibtnBookmark.setImageResource(R.drawable.baseline_bookmark_24)
            }
        }

        // 본인 옷인 경우만 수정, 삭제 이미지 버튼이 보이게 처리해서
        // 옷을 가져온 사람은 수정, 삭제 불가능
        binding.ibtnEdit.setOnClickListener {
            MaterialAlertDialogBuilder(homeActivity)
                .setTitle("옷 정보 수정")
                .setMessage("옷의 정보를 수정하시겠습니까?")
                // 왼쪽에 "취소" 배치를 위해서
                .setPositiveButton("취소", null)
                // 오른쪽에 "수정" 배치를 위해서
                .setNegativeButton("수정") { _, _ ->
                    // TODO : 수정 화면으로 이동
                }
                .show()
        }

        // 본인 옷인 경우만 수정, 삭제 이미지 버튼이 보이게 처리해서
        // 옷을 가져온 사람은 수정, 삭제 불가능
        binding.ibtnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(homeActivity)
                .setTitle("옷 삭제")
                .setMessage("옷을 삭제하시겠습니까?\n삭제 후에는 복구할 수 없습니다.")
                // 오른쪾에 "취소" 배치를 위해서
                .setPositiveButton("취소", null)
                // 오른쪽에 "삭제" 배치를 위해서
                .setNegativeButton("삭제") { _, _ ->
                    // TODO : 삭제 화면으로 이동
                }
                .show()
        }
    }

    fun registerObserve() {
        viewModel.clothesData.observe(viewLifecycleOwner) { item ->

            Log.d(TAG, "옷 상세 정보 조회 통신 결과  clothesId : ${item.clothesId}")
            Log.d(TAG, "옷 상세 정보 조회 통신 결과  photoUrl : ${item.photoUrl}")
            Log.d(TAG, "옷 상세 정보 조회 통신 결과  tags : ${item.tags}")
            Log.d(TAG, "옷 상세 정보 조회 통신 결과  clothesType : ${item.clothesType}")
            Log.d(TAG, "옷 상세 정보 조회 통신 결과  color : ${item.color}")
            Log.d(TAG, "옷 상세 정보 조회 통신 결과  isMine : ${item.isMine}")
            Log.d(TAG, "옷 상세 정보 조회 통신 결과  seasons : ${item.seasons}")
            Glide.with(binding.ivPhoto)
                .load(item.photoUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(binding.ivPhoto)

            binding.tvType.text = ClothTypeOptions.englishToKorean(item.clothesType)
            binding.tvSeasons.text = item.seasons?.joinToString(" · ")
            binding.tvColor.text = ColorOptions.englishToKorean(item.color)

            binding.cgTags.removeAllViews()
            val tags = item.tags.orEmpty()
            if (tags.isEmpty()) {
                binding.cgTags.visibility = View.GONE
            } else {
                binding.cgTags.visibility = View.VISIBLE

                // 칩 선택 대상 아님을 지정
                tags.forEach { tag ->
                    val chip = com.google.android.material.chip.Chip(homeActivity).apply {
                        text = "#$tag"
                        // 체크 상태 UI를 쓰려면 checkable 이어야 함
                        isCheckable = true
                        isChecked = true

                        // 사용자 터치는 막기
                        isClickable = false
                        isFocusable = false

                        setEnsureMinTouchTargetSize(false)

                        // 1) 기본 스타일 먼저
                        setChipDrawable(
                            com.google.android.material.chip.ChipDrawable.createFromAttributes(
                                homeActivity,
                                null,
                                0,
                                com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice
                            )
                        )

                        // 2) 체크 상태 selector 적용 (main_color 등)
                        chipBackgroundColor = homeActivity.getColorStateList(R.color.chip_bg_selector)
                        setTextColor(homeActivity.getColorStateList(R.color.chip_text_selector))
                    }

                    binding.cgTags.addView(chip)
                }
            }
            // 본인 옷이면 북마크 안 보이게 설정
            if (item.isMine == true) {
                binding.ibtnEdit.visibility = View.VISIBLE
                binding.editLinearLayout.visibility = View.VISIBLE
            } else {
                // 본인 옷이 아니면 남의 옷 가져온 상황
                isRental = true
                binding.editLinearLayout.visibility = View.GONE
                binding.ibtnBookmark.setImageResource(R.drawable.baseline_bookmark_24)
            }
        }
    }
}
