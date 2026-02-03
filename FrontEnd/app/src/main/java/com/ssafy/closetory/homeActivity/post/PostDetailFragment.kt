package com.ssafy.closetory.homeActivity.post.detail

import android.os.Bundle
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

    // 상세를 한 번이라도 로드했으면(조회수 변동), 나갈 때 목록 갱신
    private var shouldRefreshListOnExit: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (postId <= 0) {
            Toast.makeText(requireContext(), "잘못된 게시글 번호입니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        setupRecycler()
        setupSystemBack()
        observeViewModel()
        observeRefreshFromEdit()

        binding.ivPostPhoto.setOnClickListener { openPhotoDialogIfExist() }

        binding.btnUpdate.setOnClickListener {
            val args = PostCreateFragment.newEditArgs(postId)
            findNavController().navigate(R.id.action_post_detail_to_post_edit, args)
        }

        binding.btnDelete.setOnClickListener { confirmDelete() }

        // 좋아요 클릭: 상세 재조회 금지 (views 증가 버그 방지)
        binding.ivLikeIcon.setOnClickListener {
            viewModel.toggleLike(postId)
        }

        // 상세 최초 진입 때만 조회 (서버가 여기서 views+1)
        viewModel.loadPostDetail(postId, force = true)
    }

    private fun setupRecycler() {
        itemAdapter = PostDetailItemAdapter(
            onItemClick = { /* no-op */ },
            onSaveClick = { item ->
                val clothesId = item.clothesId
                if (clothesId <= 0) return@PostDetailItemAdapter
                viewModel.toggleClothesSave(postId = postId, clothesId = clothesId, willSave = !item.isSaved)
            }
        )

        binding.rvClothes.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = itemAdapter
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    private fun setupSystemBack() {
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
        // 수정 화면에서 돌아오면 상세는 재조회 + 목록도 갱신 신호 전달
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

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.message.collect { msg ->
                        if (msg.isNotBlank()) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }

                launch {
                    viewModel.postDetail.collect { detail ->
                        if (detail == null) return@collect

                        shouldRefreshListOnExit = true

                        binding.tvTitle.text = detail.title
                        binding.tvAuthor.text = detail.nickname
                        binding.tvContent.text = detail.content
                        binding.tvViews.text = detail.views.toString()
                        binding.tvLikes.text = detail.likeCount.toString()
                        binding.ivLikeIcon.isSelected = detail.isLiked

                        if (binding.ivLikeIcon.isSelected) {
                            binding.ivLikeIcon.setImageResource(R.drawable.heart_red)
                        } else {
                            binding.ivLikeIcon.setImageResource(R.drawable.heart_empty)
                        }

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

    private fun openPhotoDialogIfExist() {
        val url = currentPhotoUrl
        if (url.isNullOrBlank()) return
        PostPhotoDialogFragment.newInstance(url)
            .show(parentFragmentManager, "post_photo_dialog")
    }
}
