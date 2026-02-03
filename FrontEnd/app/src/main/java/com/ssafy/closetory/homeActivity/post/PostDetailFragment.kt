package com.ssafy.closetory.homeActivity.post.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
import com.ssafy.closetory.homeActivity.post.PostPhotoDialogFragment
import com.ssafy.closetory.homeActivity.post.PostViewModel
import com.ssafy.closetory.homeActivity.post.create.PostCreateFragment
import kotlinx.coroutines.launch

class PostDetailFragment :
    BaseFragment<FragmentPostDetailBinding>(FragmentPostDetailBinding::bind, R.layout.fragment_post_detail) {

    private val viewModel: PostViewModel by viewModels()

    private val postId: Int by lazy { arguments?.getInt("postId") ?: -1 }

    private lateinit var itemAdapter: PostDetailItemAdapter
    private var currentPhotoUrl: String? = null

    // 상세 진입 시 조회수 +1이 발생하므로, 상세를 한 번이라도 로드했다면 뒤로갈 때 목록을 갱신
    private var shouldRefreshListOnExit: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (postId <= 0) {
            Toast.makeText(requireContext(), "잘못된 게시글 번호입니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        setupItemsRecyclerView()
        observeViewModel()
        observeRefreshFromEdit()

        binding.tvContent.movementMethod = ScrollingMovementMethod()
        setupContentInnerScrollForTextView()

        binding.ivPostPhoto.setOnClickListener { openPhotoDialogIfExist() }
        binding.btnUpdate.setOnClickListener { navigateToEdit() }
        binding.btnDelete.setOnClickListener { confirmDelete() }

        // 시스템 뒤로가기 처리 (목록 갱신 신호 전달)
        setupSystemBackRefresh()

        // 상세 조회 (여기서 서버 조회수 +1이 일어나는 케이스)
        viewModel.loadPostDetail(postId, force = true)

        clickLike()
    }

    // TODO: 윤세야 좋아요 기능 해야한다!!
    private fun clickLike() {
        binding.ivLikeIcon.setOnClickListener {
        }
    }

    private fun setupSystemBackRefresh() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (shouldRefreshListOnExit) {
                        findNavController().previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("POST_REFRESH", true)
                    }
                    findNavController().popBackStack()
                }
            }
        )
    }

    private fun observeRefreshFromEdit() {
        // 수정 화면(PostCreateFragment)에서 돌아오면:
        // 1) 상세 재조회
        // 2) 목록(PostList)에도 갱신 신호 전파
        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("POST_REFRESH")
            ?.observe(viewLifecycleOwner) { refresh ->
                if (refresh == true) {
                    findNavController().previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("POST_REFRESH", true)

                    viewModel.loadPostDetail(postId, force = true)

                    findNavController()
                        .currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<Boolean>("POST_REFRESH")
                }
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupContentInnerScrollForTextView() {
        val tv = binding.tvContent
        tv.setOnTouchListener { v, event ->
            val canScroll = v.canScrollVertically(-1) || v.canScrollVertically(1)
            if (!canScroll) {
                v.parent?.requestDisallowInterceptTouchEvent(false)
                return@setOnTouchListener false
            }

            v.parent?.requestDisallowInterceptTouchEvent(true)

            if (event.actionMasked == MotionEvent.ACTION_MOVE) {
                val atTop = !v.canScrollVertically(-1)
                val atBottom = !v.canScrollVertically(1)

                val prevY = if (event.historySize > 0) event.getHistoricalY(0) else event.y
                val dy = event.y - prevY

                if (atTop && dy > 0) v.parent?.requestDisallowInterceptTouchEvent(false)
                if (atBottom && dy < 0) v.parent?.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    private fun openPhotoDialogIfExist() {
        val url = currentPhotoUrl
        if (url.isNullOrBlank()) {
            Toast.makeText(requireContext(), "이미지가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        PostPhotoDialogFragment.newInstance(url)
            .show(parentFragmentManager, "post_photo_dialog")
    }

    private fun navigateToEdit() {
        val args = PostCreateFragment.newEditArgs(postId)
        findNavController().navigate(R.id.action_post_detail_to_post_edit, args)
    }

    private fun setupItemsRecyclerView() {
        itemAdapter = PostDetailItemAdapter(
            onItemClick = { /* no-op */ },
            onSaveClick = { item ->
                val clothesId = item.clothesId
                if (clothesId <= 0) {
                    Toast.makeText(requireContext(), "옷 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                    return@PostDetailItemAdapter
                }

                viewModel.toggleClothesSave(
                    postId = postId,
                    clothesId = clothesId,
                    willSave = !item.isSaved
                )
            }
        )

        binding.rvClothes.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = itemAdapter
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.message.collect { msg ->
                        if (!msg.isNullOrBlank()) {
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                launch {
                    viewModel.postDetail.collect { detail ->
                        if (detail == null) return@collect

                        // ✅ 상세를 한 번이라도 성공적으로 받으면, 나갈 때 목록 갱신
                        shouldRefreshListOnExit = true

                        binding.tvTitle.text = detail.title
                        binding.tvAuthor.text = detail.nickname
                        binding.tvContent.text = detail.content
                        binding.tvViews.text = detail.views.toString()
                        binding.tvLikes.text = detail.likeCount.toString()
                        binding.ivLikeIcon.isSelected = detail.isLiked

                        Glide.with(this@PostDetailFragment)
                            .load(detail.profilePhotoUrl)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(binding.ivProfile)

                        Glide.with(this@PostDetailFragment)
                            .load(detail.photoUrl)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(binding.ivPostPhoto)

                        currentPhotoUrl = detail.photoUrl?.trim()

                        val loginUserId =
                            ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID)
                        val isMine = (loginUserId == detail.userId)
                        binding.layoutPostActions.visibility = if (isMine) View.VISIBLE else View.GONE

                        val hasClothes = detail.items.isNotEmpty()
                        binding.rvClothes.visibility = if (hasClothes) View.VISIBLE else View.GONE
                        binding.tvNoClothes.visibility = if (hasClothes) View.GONE else View.VISIBLE

                        itemAdapter.setIsMinePost(isMine)
                        itemAdapter.submitList(if (hasClothes) detail.items else emptyList())
                    }
                }

                launch {
                    viewModel.deleteEvent.collect { event ->
                        when (event) {
                            is PostViewModel.DeleteEvent.Success -> {
                                Toast.makeText(requireContext(), "삭제 완료", Toast.LENGTH_SHORT).show()

                                // ✅ 삭제 시에는 무조건 목록 갱신
                                findNavController().previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("POST_REFRESH", true)

                                findNavController().popBackStack()
                            }

                            is PostViewModel.DeleteEvent.Fail -> {
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle("게시글 삭제")
            .setMessage("정말 삭제할까요?")
            .setPositiveButton("삭제") { _, _ -> viewModel.deletePost(postId) }
            .setNegativeButton("취소", null)
            .show()
    }
}
