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
import com.ssafy.closetory.dto.PostDetailItemDto
import com.ssafy.closetory.homeActivity.adapter.PostDetailItemAdapter
import com.ssafy.closetory.homeActivity.post.delete.PostDeleteViewModel
import kotlinx.coroutines.launch

private const val TAG = "PostDetailFragment_싸피"

class PostDetailFragment :
    BaseFragment<FragmentPostDetailBinding>(FragmentPostDetailBinding::bind, R.layout.fragment_post_detail) {

    private val viewModel: PostDetailViewModel by viewModels()
    private val deleteViewModel: PostDeleteViewModel by viewModels()

    private val postId: Int by lazy { arguments?.getInt("postId") ?: -1 }

    private lateinit var itemAdapter: PostDetailItemAdapter

    private var currentPhotoUrl: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated() 진입, postId = $postId")

        if (postId <= 0) {
            Toast.makeText(requireContext(), "잘못된 게시글 번호입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        setupItemsRecyclerView()
        observeViewModel()
        // 게시글 내용(TextView) 내부 스크롤 활성화
        binding.tvContent.movementMethod = ScrollingMovementMethod()

        // NestedScrollView와 스크롤 충돌 방지 (내용 스크롤 → 끝이면 부모로 넘김)
        setupContentInnerScrollForTextView()

        // 좋아요 버튼 클릭
        setupLikeClicks()
        setupUpdateButton()
        setupDeleteButton()
        observeDeleteViewModel()
        setupPhotoClickDialog()

        viewModel.loadPostDetail(postId)

        binding.etComment.setOnTouchListener { v, _ ->
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
        binding.ivLikeIcon.setOnClickListener {
            // TODO
        }
    }

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

    private fun setupUpdateButton() {
        binding.btnUpdate.setOnClickListener { navigateToEdit() }
    }

    private fun navigateToEdit() {
        val bundle = Bundle().apply {
            putInt("postId", postId)
            putString("mode", "edit")
        }
        findNavController().navigate(R.id.action_post_detail_to_post_edit, bundle)
    }

    private fun setupItemsRecyclerView() {
        itemAdapter = PostDetailItemAdapter(
            onItemClick = { _ ->
                // TODO
            },
            onSaveClick = { item ->
                val willSave = !item.isSaved

                // ✅ DTO 안 건드리고도 "진짜 id"를 찾아서 사용
                val resolvedId = resolveClothesId(item)

                Log.d(TAG, "onSaveClick item=$item")
                Log.d(TAG, "resolved clothesId=$resolvedId (original clothesId=${item.clothesId})")

                viewModel.toggleClothesSave(
                    postId = postId,
                    clothesId = resolvedId,
                    willSave = willSave
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
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }

                launch {
                    viewModel.postDetail.collect { detail ->
                        if (detail == null) return@collect

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

                        val loginUserId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID)
                        val isMine = (loginUserId == detail.userId)
                        binding.layoutPostActions.visibility = if (isMine) View.VISIBLE else View.GONE

                        val hasClothes = detail.items.isNotEmpty()
                        binding.rvClothes.visibility = if (hasClothes) View.VISIBLE else View.GONE
                        binding.tvNoClothes.visibility = if (hasClothes) View.GONE else View.VISIBLE

                        itemAdapter.submitList(if (hasClothes) detail.items else emptyList())
                        itemAdapter.setIsMinePost(isMine)
                    }
                }
            }
        }
    }

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

    private fun observeDeleteViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                deleteViewModel.event.collect { event ->
                    when (event) {
                        is PostDeleteViewModel.UiEvent.DeleteSuccess -> {
                            Toast.makeText(requireContext(), "삭제 완료", Toast.LENGTH_SHORT).show()
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

    /**
     * DTO를 안 건드리고도 clothesId를 찾아내기 위한 해결책.
     *
     * 우선순위:
     * 1) item.clothesId
     * 2) getter 메서드(getId / getClothingId / getClothes_id 등)
     * 3) field(id / clothingId / clothes_id 등)
     */
    private fun resolveClothesId(item: PostDetailItemDto): Int {
        // 1) 기존 clothesId가 정상이라면 그대로 사용
        if (item.clothesId > 0) return item.clothesId

        // 2) 메서드(getId 등)에서 찾기
        val methodNames = listOf(
            "getId",
            "getClothingId",
            "getClothes_id",
            "getClothesId"
        )

        for (name in methodNames) {
            val v = runCatching {
                val m = item.javaClass.methods.firstOrNull { it.name == name && it.parameterTypes.isEmpty() }
                (m?.invoke(item) as? Number)?.toInt()
            }.getOrNull()

            if (v != null && v > 0) return v
        }

        // 3) 필드(id 등)에서 찾기
        val fieldNames = listOf("id", "clothingId", "clothes_id", "clothesId")
        for (fname in fieldNames) {
            val v = runCatching {
                val f = item.javaClass.declaredFields.firstOrNull { it.name == fname } ?: return@runCatching null
                f.isAccessible = true
                (f.get(item) as? Number)?.toInt()
            }.getOrNull()

            if (v != null && v > 0) return v
        }

        // 못 찾으면 0 유지
        return 0
    }
}
