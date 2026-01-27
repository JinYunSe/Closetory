// PostCreateFragment.kt

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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

// 등록 버튼에서 ViewModel 호출 + Multipart 변환 추가
class PostCreateFragment :
    BaseFragment<FragmentPostCreateBinding>(FragmentPostCreateBinding::bind, R.layout.fragment_post_create) {

    // HomeActivity 참조(다이얼로그/권한체크 등에 사용)
    private lateinit var homeActivity: HomeActivity

    // 카메라 런타임 권한 체크/요청 유틸
    private val cameraPermissionChecker = PermissionChecker()

    // 옷 요소(가로 스크롤) 어댑터
    private lateinit var itemAdapter: PostItemAdapter

    // 선택된 옷 요소 목록(추후 Dialog 결과로 채움)
    private val selectedItems = mutableListOf<PostCreateSelectedItem>()

    // 메인 사진(게시글 대표사진) Uri 저장
    private var selectedMainPhotoUri: Uri? = null

    private val viewModel: PostCreateViewModel by viewModels()

    // 카메라 촬영 결과를 selectedMainPhotoUri 위치에 저장하고 UI 갱신
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (!success) return@registerForActivityResult
            val uri = selectedMainPhotoUri ?: return@registerForActivityResult
            onMainPhotoSelected(uri)
        }

    // 갤러리(포토피커)에서 선택한 이미지 Uri를 받아 UI 갱신
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) return@registerForActivityResult
            onMainPhotoSelected(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 카메라 권한
        cameraPermissionChecker.init(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Fragment가 붙은 액티비티를 HomeActivity로 캐스팅
        homeActivity = requireContext() as HomeActivity

        // 옷 요소 RecyclerView(가로 스크롤/삭제) 세팅
        setupItemsRecyclerView()

        // 메인 사진 클릭 시 카메라/갤러리 선택 다이얼로그 세팅
        setupMainPhotoClick()

        // 게시글 스크롤 가로채기 방지
        setupContentInnerScroll()

        // 옷 요소 추가 버튼
        setupAddItemButton()

        // 다이얼로그 선택 결과 받기
        setupClothesPickResultListener()

        // 등록 버튼 클릭 로직
        setupCreatePostButton()

        // ViewModel 메세지 observe
        observeViewModel()
    }

    // 메인 사진을 누르면 카메라/갤러리 선택 다이얼로그를 띄움
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

    // 선택된 메인 사진 Uri를 저장하고 ImageView에 표시
    private fun onMainPhotoSelected(uri: Uri) {
        selectedMainPhotoUri = uri
        binding.ivMainPhoto.setImageURI(uri)

        // 사진 위 placeholder 텍스트가 있다면 필요 시 숨김
        // binding.tvPhotoPlaceholder.visibility = View.GONE
    }

    // 포토피커를 실행해서 갤러리에서 이미지 선택
    private fun openGalleryPicker() {
        pickImage.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    // 카메라 권한을 확인하고, 허용 시 카메라 촬영 실행
    private fun openCamera() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (cameraPermissionChecker.checkPermission(homeActivity, permissions)) {
            launchCameraToUri()
            return
        }
        cameraPermissionChecker.setOnGrantedListener { launchCameraToUri() }
        cameraPermissionChecker.requestPermissions(permissions)
    }

    // 카메라가 저장할 Uri를 만들고 TakePicture 런처 실행
    private fun launchCameraToUri() {
        selectedMainPhotoUri = createImageUri()
        selectedMainPhotoUri?.let { takePicture.launch(it) }
    }

    // FileProvider 기반으로 카메라 저장용 임시 Uri 생성
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

    // 옷 요소 RecyclerView를 가로 스크롤로 설정하고 X 버튼 삭제 동작 연결
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

    // 옷 요소 추가 버튼 클릭
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

            // 중복 선택 방지(원하면 제거 가능)
            if (selectedItems.any { it.clothesId == clothesId }) return@setFragmentResultListener

            selectedItems.add(PostCreateSelectedItem(clothesId, photoUrl))
            itemAdapter.submitList(selectedItems.toList())
        }
    }

    // DialogFragment 없이도 동작 확인을 위한 임시 더미 옷 요소 데이터 주입
    private fun loadDummyItemsForTest() {
        selectedItems.clear()
        selectedItems.addAll(
            listOf(
                PostCreateSelectedItem(1, "/media/test1.jpg"),
                PostCreateSelectedItem(2, "/media/test2.jpg"),
                PostCreateSelectedItem(3, "/media/test3.jpg"),
                PostCreateSelectedItem(4, "/media/test4.jpg"),
                PostCreateSelectedItem(5, "/media/test5.jpg"),
                PostCreateSelectedItem(6, "/media/test6.jpg")
            )
        )
        itemAdapter.submitList(selectedItems.toList()) // 어댑터에 리스트 반영
    }

    // 게시글 EditText 스크롤 가로채기 방지
    private fun setupContentInnerScroll() {
        binding.etContent.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                }

                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    v.parent?.requestDisallowInterceptTouchEvent(false)
                }
            }
            v.onTouchEvent(event)
        }
    }

    // 등록 버튼 구현 (입력 검증 + multipart로 ViewModel 호출)
    private fun setupCreatePostButton() {
        binding.btnRegistrationPost.setOnClickListener {
            val uri: Uri = selectedMainPhotoUri ?: run {
                showToast("대표 사진을 등록해주세요.")
                return@setOnClickListener
            }
            val contentText = binding.etContent.text?.toString()?.trim().orEmpty()
            val titleText = binding.etTitle.text?.toString()?.trim().orEmpty()

            // items는 selectedItems에서 clothesId만 뽑아 List<Int>로
            val itemIds: List<Int> = selectedItems.map { it.clothesId }

            when {
                uri == null -> {
                    showToast("대표 사진을 등록해주세요.")
                    return@setOnClickListener
                }

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
            Log.d("POST_CREATE_REQ", "itemIds=$itemIds") // [1,2,3]
            Log.d("POST_CREATE_REQ", "itemsJson=${Gson().toJson(itemIds)}")
            Log.d("POST_CREATE_REQ", "imageUri=$uri")

            // Uri → Multipart
            val imagePart = uriToMultipart(uri)

            // 문자열 → RequestBody
            val titleBody = titleText.toRequestBody()
            val contentBody = contentText.toRequestBody()

            // items → JSON 배열 문자열 RequestBody
            // 예: [1,2,3]
            val itemsJson = Gson().toJson(itemIds)
            val itemsBody = itemsJson.toRequestBody()

            viewModel.createPost(
                imagePart = imagePart,
                title = titleBody,
                content = contentBody,
                items = itemsBody
            )
        }
    }

    // ViewModel observe
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.message.collect { msg ->
                if (!msg.isNullOrBlank()) showToast(msg)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.createResult.collect { data ->
                if (data != null) {
                    // 등록 성공 후 화면 처리 (예: 뒤로가기, 상세 이동 등)
                    // findNavController().popBackStack()
                }
            }
        }
    }

    // Uri → MultipartBody.Part 변환
    private fun uriToMultipart(uri: Uri): MultipartBody.Part {
        val cr = requireContext().contentResolver
        val bytes = cr.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("이미지 스트림을 열 수 없습니다: $uri")

        val mime = cr.getType(uri) ?: "image/*"
        val mediaType = mime.toMediaTypeOrNull() ?: "image/*".toMediaTypeOrNull()!!

        val requestBody = RequestBody.create(mediaType, bytes)
        val fileName = "post_${System.currentTimeMillis()}.png"

        // 서버에서 파트명이 "image"인지 "photo"인지에 따라 수정 필요
        return MultipartBody.Part.createFormData("image", fileName, requestBody)
    }

    // String → RequestBody 변환
    private fun String.toRequestBody(): RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), this)
}
