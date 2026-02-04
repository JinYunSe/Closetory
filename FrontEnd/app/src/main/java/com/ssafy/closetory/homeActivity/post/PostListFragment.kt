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
    private var suppressFilterRequest = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postIdFromArgs = arguments?.getInt("postId", -1) ?: -1
        if (postIdFromArgs > 0) {
            pendingOpenPostId = postIdFromArgs
            arguments?.remove("postId")
        }

        setupRecyclerView()
        setupFilterListener()
        setupSearchListener()
        setupCreateListener()

        restoreUiState()
        observeViewModel()
        observeRefreshSignal()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (didInitialLoad) return
        didInitialLoad = true

        requestByUiState()

        pendingOpenPostId?.let {
            pendingOpenPostId = null
            goToPostDetail(it)
        }
    }

    private fun setupRecyclerView() {
        postListAdapter = PostListAdapter { item ->
            goToPostDetail(item.postId)
        }

        binding.rvPostList.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = postListAdapter
        }
    }

    private fun setupFilterListener() {
        binding.rgPostOption.setOnCheckedChangeListener { _, _ ->
            if (suppressFilterRequest) return@setOnCheckedChangeListener
            requestByUiState()
        }
    }

    private fun setupSearchListener() {
        binding.btnSearch.setOnClickListener {
            requestByUiState()
        }

        binding.etKeyword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                requestByUiState()
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

    private fun requestByUiState() {
        val keyword = binding.etKeyword.text?.toString()?.trim().orEmpty()
        val filter = getSelectedFilter()

        if (keyword.isEmpty()) {
            viewModel.loadPostsFilter(filter)
        } else {
            viewModel.loadPosts(keyword, filter)
        }
    }

    private fun getSelectedFilter(): PostQueryFilter = when (binding.rgPostOption.checkedRadioButtonId) {
        R.id.rbPopular -> PostQueryFilter.POPULAR
        R.id.rbWritten -> PostQueryFilter.WRITTEN
        R.id.rbLiked -> PostQueryFilter.LIKED
        else -> PostQueryFilter.LATEST
    }

    private fun goToPostDetail(targetPostId: Int) {
        persistUiState()
        val bundle = Bundle().apply { putInt("postId", targetPostId) }
        findNavController().navigate(R.id.action_post_list_to_post_detail, bundle)
    }

    private fun observeViewModel() {
        viewModel.postList.observe(viewLifecycleOwner) { list ->
            postListAdapter.submitList(list)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.message.collect { msg ->
                    showToast(msg)
                }
            }
        }
    }

    private fun observeRefreshSignal() {
        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("POST_REFRESH")
            ?.observe(viewLifecycleOwner) { refresh ->
                if (refresh == true) {
                    requestByUiState()
                    findNavController()
                        .currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<Boolean>("POST_REFRESH")
                }
            }
    }

    private fun persistUiState() {
        val handle = findNavController().currentBackStackEntry?.savedStateHandle ?: return
        handle["POST_FILTER_ID"] = binding.rgPostOption.checkedRadioButtonId
        handle["POST_KEYWORD"] = binding.etKeyword.text?.toString().orEmpty()
    }

    private fun restoreUiState() {
        val handle = findNavController().currentBackStackEntry?.savedStateHandle ?: return
        val filterId = handle.get<Int>("POST_FILTER_ID") ?: View.NO_ID
        val keyword = handle.get<String>("POST_KEYWORD") ?: ""

        suppressFilterRequest = true
        if (filterId != View.NO_ID) {
            binding.rgPostOption.check(filterId)
        } else {
            binding.rbLatest.isChecked = true
        }
        binding.etKeyword.setText(keyword)
        suppressFilterRequest = false
    }
}
