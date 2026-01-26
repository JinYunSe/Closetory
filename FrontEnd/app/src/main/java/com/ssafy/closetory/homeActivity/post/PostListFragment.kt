package com.ssafy.closetory.homeActivity.post

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentPostMainBinding
import com.ssafy.closetory.dto.PostQueryFilter
import com.ssafy.closetory.homeActivity.adpter.PostListAdapter
import kotlinx.coroutines.launch

class PostListFragment :
    BaseFragment<FragmentPostMainBinding>(FragmentPostMainBinding::bind, R.layout.fragment_post_main) {

    private val viewModel: PostListViewModel by viewModels()

    private lateinit var postListAdapter: PostListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        // 기본 진입 시: 최신(latest)으로 전체 목록 호출
        viewModel.loadPosts(
            keyword = null,
            filter = PostQueryFilter.LATEST
        )
    }

    private fun setupRecyclerView() {
        postListAdapter = PostListAdapter { item ->
            // TODO: 게시글 상세로 이동 처리
        }

        binding.rvPostList.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = postListAdapter
        }
    }

    private fun setupListeners() {
        // 검색 버튼 클릭 시: keyword 포함해서 요청
        binding.btnSearch.setOnClickListener {
            val keyword = binding.etKeyword.text?.toString()?.trim()
                ?.takeIf { it.isNotEmpty() }

            viewModel.loadPosts(
                keyword = keyword,
                filter = getSelectedFilter()
            )
        }

        // 라디오 변경 시: 선택된 filter로 즉시 재조회 (원치 않으면 이 부분 삭제 가능)
        binding.rgPostOption.setOnCheckedChangeListener { _, _ ->
            val keyword = binding.etKeyword.text?.toString()?.trim()
                ?.takeIf { it.isNotEmpty() }

            viewModel.loadPosts(
                keyword = keyword,
                filter = getSelectedFilter()
            )
        }
    }

    // 라디오 버튼 선택값 → PostQueryFilter 매핑
    private fun getSelectedFilter(): PostQueryFilter = when (binding.rgPostOption.checkedRadioButtonId) {
        R.id.rbLatest -> PostQueryFilter.LATEST
        R.id.rbPopular -> PostQueryFilter.POPULAR
        R.id.rbWritten -> PostQueryFilter.WRITTEN
        R.id.rbLiked -> PostQueryFilter.LIKED
        else -> PostQueryFilter.LATEST
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.postList.collect { list ->
                        postListAdapter.submitList(list)
                    }
                }

                launch {
                    viewModel.message.collect { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }

                // 로딩 UI가 있으면 여기서 처리
                // launch {
                //     viewModel.isLoading.collect { isLoading ->
                //         binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                //     }
                // }
            }
        }
    }
}
