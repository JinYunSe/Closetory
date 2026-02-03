// package com.ssafy.closetory.homeActivity.post
//
// import android.os.Bundle
// import android.view.View
// import android.widget.Toast
// import androidx.activity.OnBackPressedCallback
// import androidx.appcompat.app.AlertDialog
// import androidx.fragment.app.viewModels
// import androidx.lifecycle.Lifecycle
// import androidx.lifecycle.lifecycleScope
// import androidx.lifecycle.repeatOnLifecycle
// import androidx.navigation.fragment.findNavController
// import androidx.recyclerview.widget.LinearLayoutManager
// import com.bumptech.glide.Glide
// import com.ssafy.closetory.ApplicationClass
// import com.ssafy.closetory.R
// import com.ssafy.closetory.baseCode.base.BaseFragment
// import com.ssafy.closetory.databinding.FragmentPostDetailBinding
// import com.ssafy.closetory.homeActivity.adapter.PostDetailItemAdapter
// import com.ssafy.closetory.homeActivity.post.PostPhotoDialogFragment
// import com.ssafy.closetory.homeActivity.post.PostViewModel
// import com.ssafy.closetory.homeActivity.post.create.PostCreateFragment
// import kotlinx.coroutines.launch
//
// class PostDetailFragment :
//    BaseFragment<FragmentPostDetailBinding>(FragmentPostDetailBinding::bind, R.layout.fragment_post_detail) {
//
//    private val viewModel: PostViewModel by viewModels()
//    private val postId: Int by lazy { arguments?.getInt("postId") ?: -1 }
//
//    private lateinit var itemAdapter: PostDetailItemAdapter
//    private var currentPhotoUrl: String? = null
//
//    // 상세를 한 번이라도 로드했으면(조회수 변동), 나갈 때 목록 갱신
//    private var shouldRefreshListOnExit: Boolean = false
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        if (postId <= 0) {
//            Toast.makeText(requireContext(), "잘못된 게시글 번호입니다.", Toast.LENGTH_SHORT).show()
//            findNavController().popBackStack()
//            return
//        }
//
//        setupRecycler()
//        setupSystemBack()
//        observeViewModel()
//        observeRefreshFromEdit()
//
//        binding.ivPostPhoto.setOnClickListener { openPhotoDialogIfExist() }
//
//        binding.btnUpdate.setOnClickListener {
//            val args = PostCreateFragment.newEditArgs(postId)
//            findNavController().navigate(R.id.action_post_detail_to_post_edit, args)
//        }
//
//        binding.btnDelete.setOnClickListener { confirmDelete() }
//
//        // 좋아요 클릭: 상세 재조회 금지 (views 증가 버그 방지)
//        binding.ivLikeIcon.setOnClickListener {
//            viewModel.toggleLike(postId)
//        }
//
//        // 상세 최초 진입 때만 조회 (서버가 여기서 views+1)
//        viewModel.loadPostDetail(postId, force = true)
//    }
//
//    private fun setupRecycler() {
//        itemAdapter = PostDetailItemAdapter(
//            onItemClick = { /* no-op */ },
//            onSaveClick = { item ->
//                val clothesId = item.clothesId
//                if (clothesId <= 0) return@PostDetailItemAdapter
//                viewModel.toggleClothesSave(postId = postId, clothesId = clothesId, willSave = !item.isSaved)
//            }
//        )
//
//        binding.rvClothes.apply {
//            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//            adapter = itemAdapter
//            overScrollMode = View.OVER_SCROLL_NEVER
//        }
//    }
//
//    private fun setupSystemBack() {
//        requireActivity().onBackPressedDispatcher.addCallback(
//            viewLifecycleOwner,
//            object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    if (shouldRefreshListOnExit) {
//                        findNavController().previousBackStackEntry
//                            ?.savedStateHandle
//                            ?.set("POST_REFRESH", true)
//                    }
//                    findNavController().popBackStack()
//                }
//            }
//        )
//    }
//
//    private fun observeRefreshFromEdit() {
//        // 수정 화면에서 돌아오면 상세는 재조회 + 목록도 갱신 신호 전달
//        findNavController()
//            .currentBackStackEntry
//            ?.savedStateHandle
//            ?.getLiveData<Boolean>("POST_REFRESH")
//            ?.observe(viewLifecycleOwner) { refresh ->
//                if (refresh == true) {
//                    findNavController().previousBackStackEntry
//                        ?.savedStateHandle
//                        ?.set("POST_REFRESH", true)
//
//                    viewModel.loadPostDetail(postId, force = true)
//
//                    findNavController()
//                        .currentBackStackEntry
//                        ?.savedStateHandle
//                        ?.remove<Boolean>("POST_REFRESH")
//                }
//            }
//    }
//
//    private fun observeViewModel() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                launch {
//                    viewModel.message.collect { msg ->
//                        if (msg.isNotBlank()) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                launch {
//                    viewModel.postDetail.collect { detail ->
//                        if (detail == null) return@collect
//
//                        shouldRefreshListOnExit = true
//
//                        binding.tvTitle.text = detail.title
//                        binding.tvAuthor.text = detail.nickname
//                        binding.tvContent.text = detail.content
//                        binding.tvViews.text = detail.views.toString()
//                        binding.tvLikes.text = detail.likeCount.toString()
//                        binding.ivLikeIcon.isSelected = detail.isLiked
//
//                        if (binding.ivLikeIcon.isSelected) {
//                            binding.ivLikeIcon.setImageResource(R.drawable.heart_red)
//                        } else {
//                            binding.ivLikeIcon.setImageResource(R.drawable.heart_empty)
//                        }
//
//                        Glide.with(this@PostDetailFragment)
//                            .load(detail.profilePhotoUrl)
//                            .placeholder(R.drawable.placeholder)
//                            .error(R.drawable.placeholder)
//                            .into(binding.ivProfile)
//
//                        Glide.with(this@PostDetailFragment)
//                            .load(detail.photoUrl)
//                            .placeholder(R.drawable.placeholder)
//                            .error(R.drawable.placeholder)
//                            .into(binding.ivPostPhoto)
//
//                        currentPhotoUrl = detail.photoUrl?.trim()
//
//                        val loginUserId =
//                            ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID)
//                        val isMine = (loginUserId == detail.userId)
//                        binding.layoutPostActions.visibility = if (isMine) View.VISIBLE else View.GONE
//
//                        val hasClothes = detail.items.isNotEmpty()
//                        binding.rvClothes.visibility = if (hasClothes) View.VISIBLE else View.GONE
//                        binding.tvNoClothes.visibility = if (hasClothes) View.GONE else View.VISIBLE
//
//                        itemAdapter.setIsMinePost(isMine)
//                        itemAdapter.submitList(if (hasClothes) detail.items else emptyList())
//                    }
//                }
//
//                launch {
//                    viewModel.deleteEvent.collect { event ->
//                        when (event) {
//                            is PostViewModel.DeleteEvent.Success -> {
//                                findNavController().previousBackStackEntry
//                                    ?.savedStateHandle
//                                    ?.set("POST_REFRESH", true)
//                                findNavController().popBackStack()
//                            }
//
//                            is PostViewModel.DeleteEvent.Fail -> {
//                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun confirmDelete() {
//        AlertDialog.Builder(requireContext())
//            .setTitle("게시글 삭제")
//            .setMessage("정말 삭제할까요?")
//            .setPositiveButton("삭제") { _, _ -> viewModel.deletePost(postId) }
//            .setNegativeButton("취소", null)
//            .show()
//    }
//
//    private fun openPhotoDialogIfExist() {
//        val url = currentPhotoUrl
//        if (url.isNullOrBlank()) return
//        PostPhotoDialogFragment.newInstance(url)
//            .show(parentFragmentManager, "post_photo_dialog")
//    }
// }

// package com.ssafy.closetory.homeActivity.post
//
// import android.os.Bundle
// import android.view.View
// import android.widget.Toast
// import androidx.activity.OnBackPressedCallback
// import androidx.appcompat.app.AlertDialog
// import androidx.fragment.app.viewModels
// import androidx.lifecycle.Lifecycle
// import androidx.lifecycle.lifecycleScope
// import androidx.lifecycle.repeatOnLifecycle
// import androidx.navigation.fragment.findNavController
// import androidx.recyclerview.widget.LinearLayoutManager
// import com.bumptech.glide.Glide
// import com.ssafy.closetory.ApplicationClass
// import com.ssafy.closetory.R
// import com.ssafy.closetory.baseCode.base.BaseFragment
// import com.ssafy.closetory.databinding.FragmentPostDetailBinding
// import com.ssafy.closetory.homeActivity.adapter.PostDetailItemAdapter
// import com.ssafy.closetory.homeActivity.post.PostPhotoDialogFragment
// import com.ssafy.closetory.homeActivity.post.PostViewModel
// import com.ssafy.closetory.homeActivity.post.create.PostCreateFragment
// import kotlinx.coroutines.launch
//
// class PostDetailFragment :
//    BaseFragment<FragmentPostDetailBinding>(FragmentPostDetailBinding::bind, R.layout.fragment_post_detail) {
//
//    private val viewModel: PostViewModel by viewModels()
//    private val postId: Int by lazy { arguments?.getInt("postId") ?: -1 }
//
//    private lateinit var itemAdapter: PostDetailItemAdapter
//    private var currentPhotoUrl: String? = null
//
//    // 상세를 한 번이라도 로드했으면(조회수 변동), 나갈 때 목록 갱신
//    private var shouldRefreshListOnExit: Boolean = false
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        if (postId <= 0) {
//            Toast.makeText(requireContext(), "잘못된 게시글 번호입니다.", Toast.LENGTH_SHORT).show()
//            findNavController().popBackStack()
//            return
//        }
//
//        setupRecycler()
//        setupSystemBack()
//        observeViewModel()
//        observeRefreshFromEdit()
//
//        binding.ivPostPhoto.setOnClickListener { openPhotoDialogIfExist() }
//
//        binding.btnUpdate.setOnClickListener {
//            val args = PostCreateFragment.newEditArgs(postId)
//            findNavController().navigate(R.id.action_post_detail_to_post_edit, args)
//        }
//
//        binding.btnDelete.setOnClickListener { confirmDelete() }
//
//        // 좋아요 클릭: 상세 재조회 금지 (views 증가 버그 방지)
//        binding.ivLikeIcon.setOnClickListener {
//            viewModel.toggleLike(postId)
//        }
//
//        // 상세 최초 진입 때만 조회 (서버가 여기서 views+1)
//        viewModel.loadPostDetail(postId, force = true)
//    }
//
//    private fun setupRecycler() {
//        itemAdapter = PostDetailItemAdapter(
//            onItemClick = { /* no-op */ },
//            onSaveClick = { item ->
//                val clothesId = item.clothesId
//                if (clothesId <= 0) return@PostDetailItemAdapter
//                viewModel.toggleClothesSave(postId = postId, clothesId = clothesId, willSave = !item.isSaved)
//            }
//        )
//
//        binding.rvClothes.apply {
//            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//            adapter = itemAdapter
//            overScrollMode = View.OVER_SCROLL_NEVER
//        }
//    }
//
//    private fun setupSystemBack() {
//        requireActivity().onBackPressedDispatcher.addCallback(
//            viewLifecycleOwner,
//            object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    if (shouldRefreshListOnExit) {
//                        findNavController().previousBackStackEntry
//                            ?.savedStateHandle
//                            ?.set("POST_REFRESH", true)
//                    }
//                    findNavController().popBackStack()
//                }
//            }
//        )
//    }
//
//    private fun observeRefreshFromEdit() {
//        // 수정 화면에서 돌아오면 상세는 재조회 + 목록도 갱신 신호 전달
//        findNavController()
//            .currentBackStackEntry
//            ?.savedStateHandle
//            ?.getLiveData<Boolean>("POST_REFRESH")
//            ?.observe(viewLifecycleOwner) { refresh ->
//                if (refresh == true) {
//                    findNavController().previousBackStackEntry
//                        ?.savedStateHandle
//                        ?.set("POST_REFRESH", true)
//
//                    viewModel.loadPostDetail(postId, force = true)
//
//                    findNavController()
//                        .currentBackStackEntry
//                        ?.savedStateHandle
//                        ?.remove<Boolean>("POST_REFRESH")
//                }
//            }
//    }
//
//    private fun observeViewModel() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                launch {
//                    viewModel.message.collect { msg ->
//                        if (msg.isNotBlank()) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                launch {
//                    viewModel.postDetail.collect { detail ->
//                        if (detail == null) return@collect
//
//                        shouldRefreshListOnExit = true
//
//                        binding.tvTitle.text = detail.title
//                        binding.tvAuthor.text = detail.nickname
//                        binding.tvContent.text = detail.content
//                        binding.tvViews.text = detail.views.toString()
//                        binding.tvLikes.text = detail.likeCount.toString()
//                        binding.ivLikeIcon.isSelected = detail.isLiked
//
//                        if (binding.ivLikeIcon.isSelected) {
//                            binding.ivLikeIcon.setImageResource(R.drawable.heart_red)
//                        } else {
//                            binding.ivLikeIcon.setImageResource(R.drawable.heart_empty)
//                        }
//
//                        Glide.with(this@PostDetailFragment)
//                            .load(detail.profilePhotoUrl)
//                            .placeholder(R.drawable.placeholder)
//                            .error(R.drawable.placeholder)
//                            .into(binding.ivProfile)
//
//                        Glide.with(this@PostDetailFragment)
//                            .load(detail.photoUrl)
//                            .placeholder(R.drawable.placeholder)
//                            .error(R.drawable.placeholder)
//                            .into(binding.ivPostPhoto)
//
//                        currentPhotoUrl = detail.photoUrl?.trim()
//
//                        val loginUserId =
//                            ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID)
//                        val isMine = (loginUserId == detail.userId)
//                        binding.layoutPostActions.visibility = if (isMine) View.VISIBLE else View.GONE
//
//                        val hasClothes = detail.items.isNotEmpty()
//                        binding.rvClothes.visibility = if (hasClothes) View.VISIBLE else View.GONE
//                        binding.tvNoClothes.visibility = if (hasClothes) View.GONE else View.VISIBLE
//
//                        itemAdapter.setIsMinePost(isMine)
//                        itemAdapter.submitList(if (hasClothes) detail.items else emptyList())
//                    }
//                }
//
//                launch {
//                    viewModel.deleteEvent.collect { event ->
//                        when (event) {
//                            is PostViewModel.DeleteEvent.Success -> {
//                                findNavController().previousBackStackEntry
//                                    ?.savedStateHandle
//                                    ?.set("POST_REFRESH", true)
//                                findNavController().popBackStack()
//                            }
//
//                            is PostViewModel.DeleteEvent.Fail -> {
//                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun confirmDelete() {
//        AlertDialog.Builder(requireContext())
//            .setTitle("게시글 삭제")
//            .setMessage("정말 삭제할까요?")
//            .setPositiveButton("삭제") { _, _ -> viewModel.deletePost(postId) }
//            .setNegativeButton("취소", null)
//            .show()
//    }
//
//    private fun openPhotoDialogIfExist() {
//        val url = currentPhotoUrl
//        if (url.isNullOrBlank()) return
//        PostPhotoDialogFragment.newInstance(url)
//            .show(parentFragmentManager, "post_photo_dialog")
//    }
// }

package com.ssafy.closetory.homeActivity.post.detail

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
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
import com.ssafy.closetory.dto.CommentDto
import com.ssafy.closetory.homeActivity.adapter.CommentAdapter
import com.ssafy.closetory.homeActivity.adapter.PostDetailItemAdapter
import com.ssafy.closetory.homeActivity.mypage.MyPageService
import com.ssafy.closetory.homeActivity.post.PostPhotoDialogFragment
import com.ssafy.closetory.homeActivity.post.PostViewModel
import com.ssafy.closetory.homeActivity.post.create.PostCreateFragment
import kotlinx.coroutines.launch

class PostDetailFragment :
    BaseFragment<FragmentPostDetailBinding>(FragmentPostDetailBinding::bind, R.layout.fragment_post_detail) {
    private var myNickname: String = ""

    private val viewModel: PostViewModel by viewModels()
    private val postId: Int by lazy { arguments?.getInt("postId") ?: -1 }

    private lateinit var itemAdapter: PostDetailItemAdapter
    private lateinit var commentAdapter: CommentAdapter
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
        setupCommentsRecycler()
        setupSystemBack()
        setupCommentSubmit()
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
        viewModel.loadComments(postId)
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

    private fun setupCommentsRecycler() {
        commentAdapter = CommentAdapter(
            onEditClick = { comment -> showEditCommentDialog(comment) },
            onDeleteClick = { comment -> confirmDeleteComment(comment) }
        )

        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = commentAdapter
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    private fun setupCommentSubmit() {
        binding.btnCommentSubmit.setOnClickListener {
            val content = binding.etComment.text.toString().trim()

            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "댓글 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.createComment(postId, content)

            // 댓글 입력창 초기화
            binding.etComment.text?.clear()

            // 키보드 숨기기
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etComment.windowToken, 0)
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
                    viewModel.loadComments(postId)

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
                    viewModel.comments.collect { comments ->
                        commentAdapter.submitList(comments)

                        // 댓글이 있으면 RecyclerView 표시
                        if (comments.isNotEmpty()) {
                            binding.rvComments.visibility = View.VISIBLE
                        } else {
                            binding.rvComments.visibility = View.GONE
                        }
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

    /**
     * 댓글 수정 다이얼로그
     */
    private fun showEditCommentDialog(comment: CommentDto) {
        val editText = android.widget.EditText(requireContext()).apply {
            setText(comment.content)
            hint = "댓글 내용을 입력하세요"
            setPadding(50, 30, 50, 30)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("댓글 수정")
            .setView(editText)
            .setPositiveButton("수정") { _, _ ->
                val newContent = editText.text.toString().trim()
                if (newContent.isNotEmpty()) {
                    viewModel.updateComment(postId, comment.commentId, newContent)
                } else {
                    Toast.makeText(requireContext(), "댓글 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    /**
     * 댓글 삭제 확인 다이얼로그
     */
    private fun confirmDeleteComment(comment: CommentDto) {
        AlertDialog.Builder(requireContext())
            .setTitle("댓글 삭제")
            .setMessage("정말 삭제할까요?")
            .setPositiveButton("삭제") { _, _ ->
                viewModel.deleteComment(postId, comment.commentId)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 댓글
    private fun ensureMyNicknameLoaded(userId: Int, onReady: () -> Unit) {
        val cached = ApplicationClass.sharedPreferences.getNickname().trim()
        if (cached.isNotEmpty()) {
            myNickname = cached
            Log.d("NICK_TEST", "✅ nickname cache 사용 = [$myNickname]")
            onReady()
            return
        }

        // 캐시에 없으면 MyPage 프로필 API로 채움
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val service = ApplicationClass.retrofit.create(MyPageService::class.java)
                val res = service.getUserProfile(userId)

                if (res.isSuccessful) {
                    val nickname = res.body()?.data?.nickname?.trim().orEmpty()
                    ApplicationClass.sharedPreferences.putNickname(nickname)
                    myNickname = nickname
                    Log.d("NICK_TEST", "✅ nickname 프로필로 저장 = [$myNickname]")
                } else {
                    Log.d("NICK_TEST", "❌ 프로필 조회 실패 code=${res.code()}")
                    myNickname = ""
                }
            } catch (e: Exception) {
                Log.e("NICK_TEST", "❌ nickname 로드 예외: ${e.message}", e)
                myNickname = ""
            } finally {
                onReady()
            }
        }
    }
}
