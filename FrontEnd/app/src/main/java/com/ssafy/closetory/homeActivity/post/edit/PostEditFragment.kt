package com.ssafy.closetory.homeActivity.post.edit

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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentPostCreateBinding
import com.ssafy.closetory.dto.PostCreateSelectedItem
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.homeActivity.adapter.PostItemAdapter
import com.ssafy.closetory.homeActivity.post.create.dialog.ClothesPickerDialogFragment
import com.ssafy.closetory.util.PermissionChecker
import com.ssafy.closetory.util.image.ImageMultipartUtil
import java.io.File
import kotlin.math.abs
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

private const val TAG = "PostEditFragment_싸피"

class PostEditFragment :
    BaseFragment<FragmentPostCreateBinding>(FragmentPostCreateBinding::bind, R.layout.fragment_post_create) {

    private lateinit var homeActivity: HomeActivity
    private val cameraPermissionChecker = PermissionChecker()

    private lateinit var itemAdapter: PostItemAdapter
    private val selectedItems = mutableListOf<PostCreateSelectedItem>()

    // 기본값으로 기존 사진 Uri
    private var selectedMainPhotoUri: Uri? = null

    private val viewModel: PostEditViewModel by viewModels()

    // 사진 변경 여부 확인
    private var photoChanged = false

    // ※※※※※※※※※※※※※※※※※ postId는 arguments로 받는다고 가정 (SafeArgs면 여기만 교체)
    private val postId: Int by lazy {
        requireArguments().getInt("postId")
    }

    // 카메라 촬영 결과 Uri를 받아 메인 사진으로 반영
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (!success) return@registerForActivityResult
            val uri = selectedMainPhotoUri ?: return@registerForActivityResult
            onMainPhotoSelected(uri)
        }

    // 갤러리 선택 결과 Uri를 받아 메인 사진으로 반영
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) return@registerForActivityResult
            onMainPhotoSelected(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 카메라 권한 체크 유틸 초기화
        cameraPermissionChecker.init(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Activity 참조 세팅
        homeActivity = requireContext() as HomeActivity

        // 옷 선택 리스트 RecyclerView 초기화
        setupItemsRecyclerView()
        // 대표 사진 선택(카메라/갤러리) 클릭 이벤트 세팅
        setupMainPhotoClick()

        // 사진 텍스트 숨기기
        updateMainPhotoPlaceholder(false)

        // EditText 스크롤 충돌 방지(부모 스크롤 가로채기 방지)
        setupContentInnerScroll()

        // 옷 추가 버튼(다이얼로그 오픈) 세팅
        setupAddItemButton()

        // 옷 선택 다이얼로그 결과 수신 세팅
        setupClothesPickResultListener()

        // ✅ Edit 버튼으로 동작 (Create 버튼 리스너 대신)
        setupEditPostButton()

        // ViewModel 상태/메시지 관찰
        observeViewModel()

        // (선택) 버튼 텍스트만 바꾸고 싶으면
        // binding.btnRegistrationPost.text = "수정"
    }

    // 대표 사진 클릭 (카메라/갤러리)
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

    // "사진" placeholder 숨기기
    private fun updateMainPhotoPlaceholder(isPhotoSelected: Boolean) {
        binding.tvMainPhotoHint.visibility = if (isPhotoSelected) View.GONE else View.VISIBLE
    }

    // 사진 선택 시 Uri를 저장하고 미리보기 반영
    private fun onMainPhotoSelected(uri: Uri) {
        selectedMainPhotoUri = uri
        binding.ivMainPhoto.setImageURI(uri)
        updateMainPhotoPlaceholder(true)
        photoChanged = true
    }

    // 갤러리(포토피커) 실행
    private fun openGalleryPicker() {
        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // 카메라 권한 확인 후 촬영 실행
    private fun openCamera() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (cameraPermissionChecker.checkPermission(homeActivity, permissions)) {
            launchCameraToUri()
            return
        }
        cameraPermissionChecker.setOnGrantedListener { launchCameraToUri() }
        cameraPermissionChecker.requestPermissions(permissions)
    }

    // 카메라 저장 Uri 생성 후 TakePicture 실행
    private fun launchCameraToUri() {
        selectedMainPhotoUri = createImageUri()
        selectedMainPhotoUri?.let { takePicture.launch(it) }
    }

    // FileProvider 기반 카메라 저장용 임시 Uri 생성
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

    // 옷 선택 리스트 RecyclerView
    private fun setupItemsRecyclerView() {
        itemAdapter = PostItemAdapter().apply {
            onRemoveClickListener = { item ->
                selectedItems.remove(item)
                submitList(selectedItems.toList())
            }
        }

        binding.rvItems.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = itemAdapter
            setHasFixedSize(true)
        }
    }

    // 옷 추가 버튼 클릭 시 ClothesPickerDialogFragment 표시
    private fun setupAddItemButton() {
        binding.btnAddItem.setOnClickListener {
            ClothesPickerDialogFragment().show(parentFragmentManager, "ClothesPickerDialog")
        }
    }

    // 다이얼로그 선택 결과(clothesId, photoUrl) 수신 후 리스트 반영
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
        }
    }

    // EditText 스크롤 충돌 방지
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

                    if (!moved && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                        moved = true
                    }

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

    // 수정 버튼 클릭 -> "현재 입력값 수집 + 전송" 함수로 이동
    private fun setupEditPostButton() {
        binding.btnRegistrationPost.setOnClickListener {
            submitEditPost() // [MOVED] 기존 클릭 로직을 함수로 이동
        }
    }

    // ----------------------------
    // "현재 입력값 수집 + 멀티파트 생성 + ViewModel 호출" (기존 코드 그대로 이동)
    // ----------------------------
    private fun submitEditPost() {
        val uri: Uri? = selectedMainPhotoUri
        val titleText = binding.etTitle.text?.toString()?.trim().orEmpty()
        val contentText = binding.etContent.text?.toString()?.trim().orEmpty()
        val itemIds: List<Int> = selectedItems.map { it.clothesId }

        when {
            titleText.isBlank() -> {
                showToast("제목을 입력해주세요.")
                return
            }

            contentText.isBlank() -> {
                showToast("내용을 입력해주세요.")
                return
            }
        }

        Log.d(TAG, "POST_EDIT_REQ postId=$postId")
        Log.d(TAG, "POST_EDIT_REQ title=$titleText")
        Log.d(TAG, "POST_EDIT_REQ content=$contentText")
        Log.d(TAG, "POST_EDIT_REQ itemIds=$itemIds")
        Log.d(TAG, "POST_EDIT_REQ photoUri=$uri")

        val photo: MultipartBody.Part? =
            if (photoChanged && selectedMainPhotoUri != null) {
                ImageMultipartUtil.uriToCompressedMultipart(
                    context = requireContext(),
                    uri = selectedMainPhotoUri!!,
                    partName = "photo",
                    maxBytes = 600 * 1024,
                    maxDimension = 1280
                )
            } else {
                null
            }

        Log.d(TAG, "POST_EDIT_REQ fileHeaders=${photo?.headers}")

        viewModel.editPost(
            postId = postId,
            photo = photo,
            title = titleText,
            content = contentText,
            items = itemIds
        )
    }

    // ----------------------------
    // ✅ ViewModel observe + 프리필
    // ----------------------------
    private fun observeViewModel() {
        // 메시지 토스트
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.message.collect { msg ->
                if (!msg.isNullOrBlank()) showToast(msg)
            }
        }

        // 수정 성공 처리
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.editResult.collect { data ->
                if (data != null) {
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }
}
