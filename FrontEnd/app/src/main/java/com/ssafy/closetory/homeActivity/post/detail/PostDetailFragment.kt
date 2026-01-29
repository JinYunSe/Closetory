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
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentPostDetailBinding
import com.ssafy.closetory.homeActivity.post.create.adapter.PostDetailItemAdapter
import kotlinx.coroutines.launch

private const val TAG = "PostDetailFragment_싸피"

// 게시글 상세 페이지 Fragment
// 게시글 상세 페이지 Fragment
class PostDetailFragment :
    BaseFragment<FragmentPostDetailBinding>(FragmentPostDetailBinding::bind, R.layout.fragment_post_detail) {

    private val viewModel: PostDetailViewModel by viewModels()

    private val postId: Int by lazy { arguments?.getInt("postId") ?: -1 }

    private lateinit var itemAdapter: PostDetailItemAdapter

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

        // 게시글 상세 조회 요청
        viewModel.loadPostDetail(postId)
    }

    // 옷 요소 가로 RecyclerView 초기 세팅
    private fun setupItemsRecyclerView() {
        itemAdapter = PostDetailItemAdapter { item ->
            // 옷 요소 클릭 시 동작 처리
            Toast.makeText(requireContext(), "clothId = ${item.clothId}", Toast.LENGTH_SHORT).show()
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
                // 단발성 메시지 수신
                launch {
                    viewModel.message.collect { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }

                // 상세 데이터 수신 후 UI 반영
                launch {
                    viewModel.postDetail.collect { detail ->
                        if (detail == null) return@collect

                        binding.tvTitle.text = detail.title
                        binding.tvContent.text = detail.content
                        binding.tvCreatedAt.text = detail.createdAt
                        binding.tvViews.text = detail.views.toString()
                        binding.tvLikes.text = detail.likeCount.toString()
                        binding.btnLike.isSelected = detail.isLiked

                        // 게시글 대표 이미지 표시
                        // Glide.with(this@PostDetailFragment).load(detail.photoUrl).into(binding.ivPostPhoto)

                        // 옷 요소 리스트 표시
                        itemAdapter.submitList(detail.items)
                    }
                }
            }
        }
    }
}
