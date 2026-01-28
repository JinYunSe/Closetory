package com.ssafy.closetory.homeActivity.post.create

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentPostCreateBinding
import com.ssafy.closetory.dto.PostCreateSelectedItem
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.homeActivity.post.create.adapter.PostItemAdapter
import com.ssafy.closetory.homeActivity.post.create.dialog.ClothesPickerDialogFragment
import com.ssafy.closetory.util.PermissionChecker
import java.io.File
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

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

    // 대표 사진 Uri 저장 + ImageView에 미리보기 반영
    private fun onMainPhotoSelected(uri: Uri) {
        selectedMainPhotoUri = uri
        binding.ivMainPhoto.setImageURI(uri)
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
        binding.etContent.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN -> v.parent?.requestDisallowInterceptTouchEvent(true)

                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> v.parent?.requestDisallowInterceptTouchEvent(false)
            }
            v.onTouchEvent(event)
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

            val photoUrl = createImagePartFromUri(uri)
            val titleBody = titleText.toRequestBody("text/plain".toMediaType())
            val contentBody = contentText.toRequestBody("text/plain".toMediaType())
            val itemsBody: RequestBody? =
                itemIds.takeIf { it.isNotEmpty() }
                    ?.let { Gson().toJson(it).toRequestBody("application/json".toMediaType()) }

            viewModel.createPost(
                photoUrl = photoUrl,
                title = titleBody,
                content = contentBody,
                items = itemsBody
            )
        }
    }

    // Uri를 읽어서 MultipartBody.Part(file)로 변환
    private fun createImagePartFromUri(uri: Uri): MultipartBody.Part {
        val resolver = requireContext().contentResolver
        val mimeType = resolver.getType(uri) ?: "image/png"

        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("이미지 읽기 실패: $uri")

        val requestBody = bytes.toRequestBody(mimeType.toMediaType())

        val ext = when (mimeType) {
            "image/png" -> "png"
            "image/jpeg", "image/jpg" -> "jpg"
            else -> "png"
        }

        return MultipartBody.Part.createFormData(
            name = "photoUrl", // ⚠️ 서버 파트명에 맞춰 image/file/photo 중 하나로 수정
            filename = "post_${System.currentTimeMillis()}.$ext",
            body = requestBody
        )
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
                    // 등록 성공 후 화면 처리(예: popBackStack)
                }
            }
        }
    }
}
