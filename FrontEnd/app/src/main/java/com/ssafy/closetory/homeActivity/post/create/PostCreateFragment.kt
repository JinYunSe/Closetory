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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentPostCreateBinding
import com.ssafy.closetory.dto.PostCreateSelectedItem
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.homeActivity.post.create.adapter.PostItemAdapter
import com.ssafy.closetory.homeActivity.post.create.dialog.ClothesPickerDialogFragment
import com.ssafy.closetory.util.PermissionChecker
import com.ssafy.closetory.util.image.ImageMultipartUtil
import java.io.File
import kotlin.math.abs
import kotlinx.coroutines.launch

class PostCreateFragment :
    BaseFragment<FragmentPostCreateBinding>(FragmentPostCreateBinding::bind, R.layout.fragment_post_create) {

    private lateinit var homeActivity: HomeActivity
    private val cameraPermissionChecker = PermissionChecker()

    private lateinit var itemAdapter: PostItemAdapter
    private val selectedItems = mutableListOf<PostCreateSelectedItem>()

    private var selectedMainPhotoUri: Uri? = null

    private val viewModel: PostCreateViewModel by viewModels()

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

        // 사진 텍스트 보이기 숨기기
        updateMainPhotoPlaceholder(false)

        // EditText 스크롤 충돌 방지(부모 스크롤 가로채기 방지)
        setupContentInnerScroll()

        // 옷 추가 버튼(다이얼로그 오픈) 세팅
        setupAddItemButton()

        // 옷 선택 다이얼로그 결과 수신 세팅
        setupClothesPickResultListener()

        // 등록 버튼(멀티파트 전송) 세팅
        setupCreatePostButton()

        // ViewModel 상태/메시지 관찰
        observeViewModel()
    }

    // 대표 사진 클릭 시 카메라/갤러리 선택 다이얼로그 표시
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

    // "사진" placeholder 보이기/숨기기
    private fun updateMainPhotoPlaceholder(isPhotoSelected: Boolean) {
        binding.tvMainPhotoHint.visibility = if (isPhotoSelected) View.GONE else View.VISIBLE
    }

    // 대표 사진 Uri 저장 + ImageView에 미리보기 반영
    private fun onMainPhotoSelected(uri: Uri) {
        selectedMainPhotoUri = uri
        binding.ivMainPhoto.setImageURI(uri)
        updateMainPhotoPlaceholder(true)
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

    // 옷 선택 리스트 RecyclerView 세팅 + 삭제(X) 처리
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

    // EditText 스크롤 시 부모(NestedScrollView) 터치 가로채기 방지
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

                    // 기본은 내부 스크롤 우선
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                    false
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - startX
                    val dy = event.rawY - startY

                    if (!moved && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                        moved = true
                    }

                    // 손가락이 위로 이동(dy<0)하면 내용은 아래로 스크롤 가능해야 함(direction=1)
                    // 손가락이 아래로 이동(dy>0)하면 내용은 위로 스크롤 가능해야 함(direction=-1)
                    val direction = if (dy < 0) 1 else -1

                    val canInnerScroll = v.canScrollVertically(direction)

                    // 내부에서 더 스크롤 가능하면 부모 가로채기 금지, 끝이면 부모에게 넘김
                    v.parent?.requestDisallowInterceptTouchEvent(canInnerScroll)

                    false
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // 이동이 거의 없으면 탭으로 처리 -> 포커스/키보드
                    if (!moved) v.performClick()

                    // 다음 터치를 위해 원복
                    v.parent?.requestDisallowInterceptTouchEvent(false)
                    false
                }

                else -> false
            }
        }
    }

    // 등록 버튼 클릭 시 입력 검증 후 멀티파트로 ViewModel 호출
    private fun setupCreatePostButton() {
        binding.btnRegistrationPost.setOnClickListener {
            val uri: Uri = selectedMainPhotoUri ?: run {
                showToast("대표 사진을 등록해주세요.")
                return@setOnClickListener
            }

            val titleText = binding.etTitle.text?.toString()?.trim().orEmpty()
            val contentText = binding.etContent.text?.toString()?.trim().orEmpty()
            val itemIds: List<Int> = selectedItems.map { it.clothesId }

            when {
                titleText.isBlank() -> {
                    showToast("제목을 입력해주세요.")
                    return@setOnClickListener
                }

                contentText.isBlank() -> {
                    showToast("내용을 입력해주세요.")
                    return@setOnClickListener
                }
            }

            Log.d("POST_CREATE_REQ", "title=$titleText")
            Log.d("POST_CREATE_REQ", "content=$contentText")
            Log.d("POST_CREATE_REQ", "itemIds=$itemIds")
            Log.d("POST_CREATE_REQ", "photoUrl=$uri")

            val photo = ImageMultipartUtil.uriToCompressedMultipart(
                context = requireContext(),
                uri = uri,
                partName = "photo", // 서버에서 받는 키
                maxBytes = 600 * 1024, // 600KB 제한
                maxDimension = 1280 // 최대 해상도
            )
            // 파일 파트 name/filename 확인 로그
            Log.d("POST_CREATE_REQ", "fileHeaders=${photo.headers}")

            val title = binding.etTitle.text.toString()
            val content = binding.etContent.text.toString()

            val items = mutableListOf<Int>()
            for (item in selectedItems) {
                items.add(item.clothesId)
            }

            viewModel.createPost(
                photo = photo,
                title = title,
                content = content,
                items = items
            )
        }
    }

    // ViewModel Flow를 수신해 토스트/성공 후처리 수행
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.message.collect { msg ->
                if (!msg.isNullOrBlank()) showToast(msg)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.createResult.collect { data ->
                if (data != null) {
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }
}
