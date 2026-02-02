// PostDetailFragment.kt
package com.ssafy.closetory.homeActivity.post.detail

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.ssafy.closetory.homeActivity.post.delete.PostDeleteViewModel
import kotlinx.coroutines.launch

private const val TAG = "PostDetailFragment_싸피"

// 게시글 상세 페이지 Fragment
class PostDetailFragment :
    BaseFragment<FragmentPostDetailBinding>(FragmentPostDetailBinding::bind, R.layout.fragment_post_detail) {

    // ViewModel 등록
    private val viewModel: PostDetailViewModel by viewModels()
    private val deleteViewModel: PostDeleteViewModel by viewModels()

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

        // 게시글 내용(TextView) 내부 스크롤 활성화
        binding.tvContent.movementMethod = ScrollingMovementMethod()

        // NestedScrollView와 스크롤 충돌 방지 (내용 스크롤 → 끝이면 부모로 넘김)
        setupContentInnerScrollForTextView()

        // 좋아요 버튼 클릭
        setupLikeClicks()

        // 수정 버튼 클릭
        setupUpdateButton()

        // 삭제 버튼 클릭
        setupDeleteButton()

        // 삭제 결과 이벤트 수신
        observeDeleteViewModel()

        // 사진 클릭 시 다이얼로그
        setupPhotoClickDialog()

        // 게시글 상세 조회 요청
        viewModel.loadPostDetail(postId)

        // 댓글 스크롤과 NestedScrollView와 충돌 방지
        binding.etComment.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    private fun setupContentInnerScrollForTextView() {
        val tv = binding.tvContent

        tv.setOnTouchListener { v, event ->
            // 내용이 스크롤 가능한 경우에만 부모 스크롤 막기
            val canScroll = v.canScrollVertically(-1) || v.canScrollVertically(1)
            if (!canScroll) {
                v.parent?.requestDisallowInterceptTouchEvent(false)
                return@setOnTouchListener false
            }

            v.parent?.requestDisallowInterceptTouchEvent(true)

            if (event.actionMasked == MotionEvent.ACTION_MOVE) {
                val atTop = !v.canScrollVertically(-1)
                val atBottom = !v.canScrollVertically(1)

                // 손가락 이동 방향 추정 (history 없으면 dy=0이라 전환이 덜 민감해짐)
                val prevY = if (event.historySize > 0) event.getHistoricalY(0) else event.y
                val dy = event.y - prevY

                // 위 끝에서 더 위로(손가락 아래로 dy>0) 당기면 부모에게 넘김
                if (atTop && dy > 0) v.parent?.requestDisallowInterceptTouchEvent(false)

                // 아래 끝에서 더 아래로(손가락 위로 dy<0) 밀면 부모에게 넘김
                if (atBottom && dy < 0) v.parent?.requestDisallowInterceptTouchEvent(false)
            }

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

    // 사진 클릭 시 다이얼로그
    private fun setupPhotoClickDialog() {
        binding.ivPostPhoto.setOnClickListener {
            val url = currentPhotoUrl

            if (url.isNullOrBlank()) {
                Toast.makeText(requireContext(), "이미지가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            PostPhotoDialogFragment
                .newInstance(url)
                .show(parentFragmentManager, "post_photo_dialog")
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

                        // 현재 게시글 대표 이미지 URL 변수 저장 (나중에 터치 다이얼로그 쓰기 위함)
                        currentPhotoUrl = detail.photoUrl?.trim()

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

    // 삭제 버튼 클릭
    private fun setupDeleteButton() {
        binding.btnDelete.setOnClickListener {
            if (postId <= 0) {
                Toast.makeText(requireContext(), "잘못된 게시글 번호입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(requireContext())
                .setTitle("게시글 삭제")
                .setMessage("정말 삭제할까요?")
                .setPositiveButton("삭제") { _, _ ->
                    deleteViewModel.deletePost(postId)
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    // 삭제 결과 이벤트 수신
    private fun observeDeleteViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                deleteViewModel.event.collect { event ->
                    when (event) {
                        is PostDeleteViewModel.UiEvent.DeleteSuccess -> {
                            Toast.makeText(requireContext(), "삭제 완료", Toast.LENGTH_SHORT).show()

                            // 상세 화면 종료 -> 목록 화면으로 복귀
                            findNavController().popBackStack()
                        }

                        is PostDeleteViewModel.UiEvent.DeleteFail -> {
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}
