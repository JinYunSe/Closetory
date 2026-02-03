package com.ssafy.closetory.homeActivity.post.create

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentPostCreateBinding
import com.ssafy.closetory.dto.PostCreateSelectedItem
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.homeActivity.adapter.PostItemAdapter
import com.ssafy.closetory.homeActivity.post.ClothesPickerDialogFragment
import com.ssafy.closetory.homeActivity.post.PostViewModel
import com.ssafy.closetory.util.PermissionChecker
import com.ssafy.closetory.util.image.ImageMultipartUtil
import java.io.File
import kotlin.math.abs
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

private const val TAG = "PostCreateFragment_싸피"

class PostCreateFragment :
    BaseFragment<FragmentPostCreateBinding>(FragmentPostCreateBinding::bind, R.layout.fragment_post_create) {

    companion object {
        private const val ARG_MODE = "mode"
        private const val ARG_POST_ID = "postId"

        const val MODE_CREATE = "create"
        const val MODE_EDIT = "edit"

        fun newCreateArgs(): Bundle = Bundle().apply {
            putString(ARG_MODE, MODE_CREATE)
            putInt(ARG_POST_ID, -1)
        }

        fun newEditArgs(postId: Int): Bundle = Bundle().apply {
            putString(ARG_MODE, MODE_EDIT)
            putInt(ARG_POST_ID, postId)
        }
    }

    private val mode: String by lazy { arguments?.getString(ARG_MODE) ?: MODE_CREATE }
    private val postId: Int by lazy { arguments?.getInt(ARG_POST_ID) ?: -1 }

    private lateinit var homeActivity: HomeActivity
    private val viewModel: PostViewModel by viewModels()

    private val cameraPermissionChecker = PermissionChecker()

    private var selectedMainPhotoUri: Uri? = null
    private var isPhotoChanged: Boolean = false

    private lateinit var itemAdapter: PostItemAdapter
    private val selectedItems = mutableListOf<PostCreateSelectedItem>()

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (!success) return@registerForActivityResult
            val uri = selectedMainPhotoUri ?: return@registerForActivityResult
            onMainPhotoSelected(uri, fromUser = true)
        }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) return@registerForActivityResult
            onMainPhotoSelected(uri, fromUser = true)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraPermissionChecker.init(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireContext() as HomeActivity

        setupItemsRecyclerView()
        setupMainPhotoClick()
        setupContentInnerScroll()
        setupAddItemButton()
        setupClothesPickResultListener()
        updateItemsEmptyUi()

        applyModeUi()
        setupSubmitButton()
        collectVmEvents()

        if (mode == MODE_EDIT) {
            if (postId <= 0) {
                showToast("잘못된 게시글 번호입니다.")
                return
            }
            viewModel.loadPostDetail(postId, force = true)
        } else {
            updateMainPhotoPlaceholder(false)
        }
    }

    private fun applyModeUi() {
        binding.btnRegistrationPost.text = if (mode == MODE_EDIT) "수정" else "등록"
    }

    private fun setupMainPhotoClick() {
        binding.ivMainPhoto.setOnClickListener {
            AlertDialog.Builder(homeActivity)
                .setTitle("사진 가져오기")
                .setItems(arrayOf("카메라 촬영", "갤러리 선택")) { _, which ->
                    when (which) {
                        0 -> openCamera()
                        1 -> openGalleryPicker()
                    }
                }
                .show()
        }
    }

    private fun updateMainPhotoPlaceholder(isPhotoSelected: Boolean) {
        binding.tvMainPhotoHint.visibility = if (isPhotoSelected) View.GONE else View.VISIBLE
    }

    private fun onMainPhotoSelected(uri: Uri, fromUser: Boolean) {
        selectedMainPhotoUri = uri
        binding.ivMainPhoto.setImageURI(uri)
        updateMainPhotoPlaceholder(true)
        if (fromUser) isPhotoChanged = true
    }

    private fun openGalleryPicker() {
        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun openCamera() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (cameraPermissionChecker.checkPermission(homeActivity, permissions)) {
            launchCameraToUri()
            return
        }
        cameraPermissionChecker.setOnGrantedListener { launchCameraToUri() }
        cameraPermissionChecker.requestPermissions(permissions)
    }

    private fun launchCameraToUri() {
        selectedMainPhotoUri = createImageUri()
        selectedMainPhotoUri?.let { takePicture.launch(it) }
    }

    private fun createImageUri(): Uri? = try {
        val dir = File(requireContext().cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "post_${System.currentTimeMillis()}.png")
        FileProvider.getUriForFile(
            requireContext(),
            "${homeActivity.packageName}.fileprovider",
            file
        )
    } catch (_: Exception) {
        null
    }

    private fun setupItemsRecyclerView() {
        itemAdapter = PostItemAdapter().apply {
            onRemoveClickListener = { item ->
                selectedItems.remove(item)
                submitList(selectedItems.toList())
                updateItemsEmptyUi()
            }
        }

        binding.rvItems.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = itemAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupAddItemButton() {
        binding.btnAddItem.setOnClickListener {
            ClothesPickerDialogFragment().show(parentFragmentManager, "ClothesPickerDialog")
        }
    }

    private fun setupClothesPickResultListener() {
        parentFragmentManager.setFragmentResultListener(
            ClothesPickerDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val clothesId = bundle.getInt(ClothesPickerDialogFragment.KEY_CLOTHES_ID)
            val photoUrl =
                bundle.getString(ClothesPickerDialogFragment.KEY_PHOTO_URL) ?: return@setFragmentResultListener

            if (selectedItems.any { it.clothesId == clothesId }) return@setFragmentResultListener

            selectedItems.add(PostCreateSelectedItem(clothesId, photoUrl))
            itemAdapter.submitList(selectedItems.toList())
            updateItemsEmptyUi()
        }
    }

    private fun updateItemsEmptyUi() {
        val hasItems = selectedItems.isNotEmpty()
        binding.rvItems.visibility = if (hasItems) View.VISIBLE else View.GONE
        binding.tvNoClothes.visibility = if (hasItems) View.GONE else View.VISIBLE
    }

    private fun setupContentInnerScroll() {
        val et = binding.etContent
        val touchSlop = ViewConfiguration.get(requireContext()).scaledTouchSlop

        var startX = 0f
        var startY = 0f
        var moved = false

        et.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX
                    startY = event.rawY
                    moved = false
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                    false
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - startX
                    val dy = event.rawY - startY
                    if (!moved && (abs(dx) > touchSlop || abs(dy) > touchSlop)) moved = true

                    val direction = if (dy < 0) 1 else -1
                    val canInnerScroll = v.canScrollVertically(direction)
                    v.parent?.requestDisallowInterceptTouchEvent(canInnerScroll)
                    false
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (!moved) v.performClick()
                    v.parent?.requestDisallowInterceptTouchEvent(false)
                    false
                }

                else -> false
            }
        }
    }

    private fun setupSubmitButton() {
        binding.btnRegistrationPost.setOnClickListener {
            val titleText = binding.etTitle.text?.toString()?.trim().orEmpty()
            val contentText = binding.etContent.text?.toString()?.trim().orEmpty()
            val items = selectedItems.map { it.clothesId }

            when {
                titleText.isBlank() -> return@setOnClickListener showToast("제목을 입력해주세요.")
                titleText.length > 30 -> return@setOnClickListener showToast("제목은 30자 이하로 입력해주세요.")
                contentText.isBlank() -> return@setOnClickListener showToast("내용을 입력해주세요.")
            }

            if (mode == MODE_CREATE) {
                val uri = selectedMainPhotoUri ?: return@setOnClickListener showToast("대표 사진을 등록해주세요.")
                val photo = buildPhotoPartOrNull(uri) ?: return@setOnClickListener showToast("사진 처리 실패")

                viewModel.createPost(
                    photo = photo,
                    title = titleText,
                    content = contentText,
                    items = items
                )
                return@setOnClickListener
            }

            if (postId <= 0) return@setOnClickListener showToast("잘못된 게시글 번호입니다.")

            val photoPart: MultipartBody.Part? =
                if (isPhotoChanged) {
                    val uri = selectedMainPhotoUri ?: return@setOnClickListener showToast("사진 처리 실패")
                    buildPhotoPartOrNull(uri)
                } else {
                    null
                }

            viewModel.editPost(
                postId = postId,
                photo = photoPart,
                title = titleText,
                content = contentText,
                items = items
            )
        }
    }

    private fun buildPhotoPartOrNull(uri: Uri): MultipartBody.Part? = try {
        ImageMultipartUtil.uriToCompressedMultipart(
            context = requireContext(),
            uri = uri,
            partName = "photo",
            maxBytes = 600 * 1024,
            maxDimension = 1280
        )
    } catch (e: Exception) {
        Log.e(TAG, "buildPhotoPartOrNull error", e)
        null
    }

    private fun collectVmEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.message.collect { msg ->
                        if (!msg.isNullOrBlank()) showToast(msg)
                    }
                }

                launch {
                    viewModel.createResult.collect { data ->
                        if (data != null) {
                            // ✅ 핵심: 목록(또는 상세)로 돌아가면 즉시 갱신되도록 신호 전달
                            findNavController().previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("POST_REFRESH", true)
                            findNavController().popBackStack()
                        }
                    }
                }

                launch {
                    viewModel.editResult.collect { data ->
                        if (data != null) {
                            // ✅ 핵심: 상세로 돌아가면 상세/목록이 모두 갱신되도록 신호 전달
                            // (상세가 이 신호를 받아 목록에도 전파함)
                            findNavController().previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("POST_REFRESH", true)
                            findNavController().popBackStack()
                        }
                    }
                }

                launch {
                    viewModel.postDetail.collect { detail ->
                        if (detail == null) return@collect
                        if (mode != MODE_EDIT) return@collect

                        binding.etTitle.setText(detail.title ?: "")
                        binding.etContent.setText(detail.content ?: "")

                        val url = detail.photoUrl?.trim()
                        if (!url.isNullOrBlank()) {
                            Glide.with(this@PostCreateFragment)
                                .load(url)
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.placeholder)
                                .into(binding.ivMainPhoto)
                            updateMainPhotoPlaceholder(true)
                            isPhotoChanged = false
                        } else {
                            updateMainPhotoPlaceholder(false)
                        }

                        selectedItems.clear()
                        detail.items.forEach { item ->
                            val cid = item.clothesId
                            val purl = item.photoUrl ?: ""
                            if (cid > 0 && purl.isNotBlank()) {
                                selectedItems.add(PostCreateSelectedItem(cid, purl))
                            }
                        }
                        itemAdapter.submitList(selectedItems.toList())
                        updateItemsEmptyUi()
                    }
                }
            }
        }
    }
}
