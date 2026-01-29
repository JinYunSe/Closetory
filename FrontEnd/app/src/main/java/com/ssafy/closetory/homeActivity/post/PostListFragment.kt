package com.ssafy.closetory.homeActivity.post

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
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
import com.ssafy.closetory.homeActivity.adpter.PostListAdapter
import kotlinx.coroutines.launch

private const val TAG = "PostListFragment_싸피"
class PostListFragment :
    BaseFragment<FragmentPostMainBinding>(FragmentPostMainBinding::bind, R.layout.fragment_post_main) {

    private val viewModel: PostListViewModel by viewModels()

    private lateinit var postListAdapter: PostListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupPostOptionRadios()

        // 검색, 버튼 리스너
        setupListeners()

        observeViewModel()

        // 기본 진입 시: 최신(latest)으로 전체 목록 호출
        requestPosts(keyword = null)
    }

    // RecyclerView(게시글 카드 목록) 초기 세팅
    private fun setupRecyclerView() {
        postListAdapter = PostListAdapter { item ->
            // TODO: 게시글 상세로 이동 처리
        }

        binding.rvPostList.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = postListAdapter
        }
    }

    // 2x2 라디오 버튼을 "4개 중 1개만 선택"되도록 강제 + 기본 선택(최신순)
    private fun setupPostOptionRadios() {
        val radios = getPostOptionRadios()

        fun checkOnly(target: RadioButton) {
            radios.forEach { it.isChecked = (it == target) }
        }

        // 기본 선택: 최신순
        checkOnly(binding.rbLatest)

        // 라디오 버튼 클릭 시: 하나만 체크되게 강제하고 즉시 재조회
        radios.forEach { rb ->
            rb.setOnClickListener {
                checkOnly(rb)
                requestPosts(keyword = getKeywordOrNull())
            }
        }
    }

    // 검색 버튼 등 "사용자 액션" 리스너 모음
    private fun setupListeners() {
        // 검색 버튼 클릭 시: keyword 포함해서 요청
        binding.btnSearch.setOnClickListener {
            requestPosts(keyword = getKeywordOrNull())
        }

        // 게시글 생성 버튼 클릭 시: 게시글 생성 화면으로 이동
        binding.btnCreatePost.setOnClickListener {
            findNavController().navigate(R.id.action_post_list_to_post_create)
        }
    }

    // keyword + filter 기준으로 게시글 목록 조회를 요청하는 공통 함수
    private fun requestPosts(keyword: String?) {
        viewModel.loadPosts(
            keyword = keyword,
            filter = getSelectedFilter()
        )
    }

    // 검색어 EditText에서 문자열을 가져오되, 빈 값이면 null 처리
    private fun getKeywordOrNull(): String? = binding.etKeyword.text?.toString()?.trim()
        ?.takeIf { it.isNotEmpty() }

    // 2x2 라디오 버튼 4개 리스트 반환 (편의 함수)
    private fun getPostOptionRadios(): List<RadioButton> = listOf(
        binding.rbLatest,
        binding.rbPopular,
        binding.rbWritten,
        binding.rbLiked
    )

    // 현재 체크된 라디오 버튼을 PostQueryFilter로 변환
    // (RadioGroup의 checkedRadioButtonId는 2x2 구조에서 신뢰하기 어려워서, isChecked로 판단)
    private fun getSelectedFilter(): PostQueryFilter = when {
        binding.rbPopular.isChecked -> PostQueryFilter.POPULAR
        binding.rbWritten.isChecked -> PostQueryFilter.WRITTEN
        binding.rbLiked.isChecked -> PostQueryFilter.LIKED
        else -> PostQueryFilter.LATEST
    }

    // ViewModel 상태 관찰: 게시글 리스트 갱신 + 메시지 표시
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 게시글 목록 갱신
                launch {
                    viewModel.postList.collect { list ->
                        postListAdapter.submitList(list)
                    }
                }

                // 에러/안내 메시지 표시
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
