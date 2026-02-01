// PostDetailFragment.kt
package com.ssafy.closetory.homeActivity.post.detail

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentPostDetailBinding
import com.ssafy.closetory.homeActivity.adapter.PostDetailItemAdapter
import kotlinx.coroutines.launch

private const val TAG = "PostDetailFragment_싸피"

// 게시글 상세 페이지 Fragment
class PostDetailFragment :
    BaseFragment<FragmentPostDetailBinding>(FragmentPostDetailBinding::bind, R.layout.fragment_post_detail) {

    private val viewModel: PostDetailViewModel by viewModels()

    private val postId: Int by lazy { arguments?.getInt("postId") ?: -1 }

    private lateinit var itemAdapter: PostDetailItemAdapter

    // 대표 이미지 URL을 저장
    private var currentPhotoUrl: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated() 진입, postId = $postId")

        // postId 입력 검증
        if (postId <= 0) {
            Toast.makeText(requireContext(), "잘못된 게시글 번호입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 옷 요소 RecyclerView 초기 세팅
        setupItemsRecyclerView()

        // ViewModel 상태 관찰
        observeViewModel()

        // 좋아요 버튼 클릭
        setupLikeClicks()

        // 수정 버튼 클릭
        setupUpdateButton()

        // 게시글 상세 조회 요청
        viewModel.loadPostDetail(postId)

        // 댓글 스크롤과 NestedScrollView와 충돌 방지
        binding.etComment.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    // 좋아요 버튼 클릭
    private fun setupLikeClicks() {
        // 좋아요 버튼 클릭
        binding.ivLikeIcon.setOnClickListener {
            // TODO() : 좋아요 기능 구현 필요
        }
    }

    // 수정 버튼 클릭
    private fun setupUpdateButton() {
        binding.btnUpdate.setOnClickListener {
            navigateToEdit()
        }
    }
    private fun navigateToEdit() {
        val bundle = Bundle().apply {
            putInt("postId", postId) // 수정할 게시글 id 전달
            putString("mode", "edit") // create/edit 구분
        }
        findNavController().navigate(R.id.action_post_detail_to_post_edit, bundle)
    }

    // 옷 요소 가로 RecyclerView 초기 세팅
    private fun setupItemsRecyclerView() {
        itemAdapter = PostDetailItemAdapter { item ->
            // TODO: 옷 요소 터치했을 때 사용 동작
        }

        binding.rvClothes.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = itemAdapter
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    // 게시글 상세 데이터와 메시지 상태 관찰
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.message.collect { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }

                // 상세 데이터 수신 후 UI 반영
                launch {
                    viewModel.postDetail.collect { detail ->
                        if (detail == null) return@collect

                        // 텍스트 바인딩
                        binding.tvTitle.text = detail.title
                        binding.tvAuthor.text = detail.nickname
                        binding.tvContent.text = detail.content
                        binding.tvViews.text = detail.views.toString()
                        binding.tvLikes.text = detail.likeCount.toString()
                        binding.ivLikeIcon.isSelected = detail.isLiked

                        // 프로필 이미지
                        Glide.with(this@PostDetailFragment)
                            .load(detail.profilePhotoUrl)
                            .placeholder(R.drawable.placeholder) // 너 프로젝트 placeholder로 맞춰
                            .error(R.drawable.placeholder)
                            .into(binding.ivProfile)

                        // 게시글 대표 이미지
                        Glide.with(this@PostDetailFragment)
                            .load(detail.photoUrl)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(binding.ivPostPhoto)

                        // 내 글인지 판별 → 수정/삭제 버튼 표시
                        val loginUserId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID)
                        Log.d(TAG, "현재 로그인 ID: $loginUserId ")
                        val isMine = (loginUserId == detail.userId)
                        Log.d(TAG, "detail.userId = ${detail.userId}")
                        Log.d(TAG, "게시글 사용자인지 판별하기 : $isMine")
                        binding.layoutPostActions.visibility = if (isMine) View.VISIBLE else View.GONE

                        val hasClothes = detail.items.isNotEmpty()

                        // 옷 요소가 없으면 "옷 정보가 없습니다" 표시 해주기
                        binding.rvClothes.visibility = if (hasClothes) View.VISIBLE else View.GONE
                        binding.tvNoClothes.visibility = if (hasClothes) View.GONE else View.VISIBLE

                        if (hasClothes) {
                            itemAdapter.submitList(detail.items)
                        } else {
                            itemAdapter.submitList(emptyList())
                        }
                        // 내 게시글인지 확인 후 숨기기.
                        itemAdapter.setIsMinePost(isMine)
                    }
                }
            }
        }
    }
}
