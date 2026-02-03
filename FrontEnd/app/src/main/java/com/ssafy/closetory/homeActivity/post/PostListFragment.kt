package com.ssafy.closetory.homeActivity.post

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentPostMainBinding
import com.ssafy.closetory.dto.PostQueryFilter
import com.ssafy.closetory.homeActivity.adapter.PostListAdapter
import com.ssafy.closetory.homeActivity.post.create.PostCreateFragment
import kotlinx.coroutines.launch

class PostListFragment :
    BaseFragment<FragmentPostMainBinding>(FragmentPostMainBinding::bind, R.layout.fragment_post_main) {

    private val viewModel: PostViewModel by viewModels()

    private lateinit var postListAdapter: PostListAdapter

    private var didInitialLoad = false
    private var pendingOpenPostId: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Home 등에서 postId가 넘어오면 목록 뜬 직후 상세로 이동
        val postIdFromArgs = arguments?.getInt("postId", -1) ?: -1
        if (postIdFromArgs > 0) {
            pendingOpenPostId = postIdFromArgs
            arguments?.remove("postId")
        }

        setupRecyclerView()
        setupFilterListener()
        setupSearchListener()
        setupCreateListener()
        observeViewModel()
        observeRefreshSignal()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (didInitialLoad) return
        didInitialLoad = true

        requestPosts()

        pendingOpenPostId?.let {
            pendingOpenPostId = null
            goToPostDetail(it)
        }
    }

    // -------------------------
    // UI Setup
    // -------------------------
    private fun setupRecyclerView() {
        postListAdapter = PostListAdapter { item ->
            goToPostDetail(item.postId)
        }

        binding.rvPostList.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = postListAdapter
        }
    }

    /**
     * RadioGroup으로 단일 선택을 안정적으로 처리
     * (RadioButton에 onClick + 수동 체크 해제 방식은 동작 꼬일 수 있음)
     */
    private fun setupFilterListener() {
        // 기본값(최신순) 보장
        if (binding.rgPostOption.checkedRadioButtonId == View.NO_ID) {
            binding.rbLatest.isChecked = true
        }

        binding.rgPostOption.setOnCheckedChangeListener { _, _ ->
            // 필터 바뀌면 즉시 조회
            requestPosts()
        }
    }

    private fun setupSearchListener() {
        binding.btnSearch.setOnClickListener {
            requestPosts()
        }

        binding.etKeyword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                requestPosts()
                ViewCompat.getWindowInsetsController(binding.etKeyword)
                    ?.hide(WindowInsetsCompat.Type.ime())
                true
            } else {
                false
            }
        }
    }

    private fun setupCreateListener() {
        binding.btnCreatePost.setOnClickListener {
            val args = PostCreateFragment.newCreateArgs()
            findNavController().navigate(R.id.action_post_list_to_post_create, args)
        }
    }

    // -------------------------
    // Navigation
    // -------------------------
    private fun goToPostDetail(targetPostId: Int) {
        val bundle = Bundle().apply { putInt("postId", targetPostId) }
        findNavController().navigate(R.id.action_post_list_to_post_detail, bundle)
    }

    // -------------------------
    // Data
    // -------------------------
    private fun requestPosts() {
        val keyword = binding.etKeyword.text?.toString()?.trim().takeIf { !it.isNullOrBlank() }
        viewModel.loadPosts(keyword = keyword, filter = getSelectedFilter())
        // PostViewModel에서 keyword가 비어있으면 getPostsFilter로 분기하도록 되어 있으니 그대로 사용하면 됨
    }

    private fun getSelectedFilter(): PostQueryFilter = when (binding.rgPostOption.checkedRadioButtonId) {
        R.id.rbPopular -> PostQueryFilter.POPULAR
        R.id.rbWritten -> PostQueryFilter.WRITTEN
        R.id.rbLiked -> PostQueryFilter.LIKED
        else -> PostQueryFilter.LATEST
    }

    // -------------------------
    // Observe
    // -------------------------
    private fun observeViewModel() {
        // LiveData observe
        viewModel.postList.observe(viewLifecycleOwner) { list ->
            postListAdapter.submitList(list)
        }

        // SharedFlow collect
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.message.collect { msg ->
                    showToast(msg)
                }
            }
        }
    }

    /**
     * 상세/등록/수정/삭제 후 목록으로 돌아왔을 때 즉시 갱신
     */
    private fun observeRefreshSignal() {
        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("POST_REFRESH")
            ?.observe(viewLifecycleOwner) { refresh ->
                if (refresh == true) {
                    requestPosts()
                    findNavController()
                        .currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<Boolean>("POST_REFRESH")
                }
            }
    }
}
