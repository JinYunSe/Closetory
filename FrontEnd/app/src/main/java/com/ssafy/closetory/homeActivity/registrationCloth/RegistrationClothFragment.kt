package com.ssafy.closetory.homeActivity.registrationCloth

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentRegistrationClothBinding
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.homeActivity.closet.ClothesDetailFragment
import com.ssafy.closetory.util.ClothTypeOptions
import com.ssafy.closetory.util.ColorOptions
import com.ssafy.closetory.util.PermissionChecker
import com.ssafy.closetory.util.SeasonOptions
import com.ssafy.closetory.util.TagOptions
import com.ssafy.closetory.util.image.ImageMultipartUtil
import java.io.File
import kotlinx.coroutines.launch

private const val TAG = "RegistrationClothFragme_싸피"

class RegistrationClothFragment :
    BaseFragment<FragmentRegistrationClothBinding>(
        FragmentRegistrationClothBinding::bind,
        R.layout.fragment_registration_cloth
    ) {

    private lateinit var homeActivity: HomeActivity
    private val cameraPermissionChecker = PermissionChecker()

    private lateinit var colorAdapter: ColorOptions.ColorAdapter
    private lateinit var tagsSection: View
    private lateinit var seasonSection: View
    private lateinit var clothTypeSection: View
    private lateinit var colorSection: View

    private var selectedImageUri: Uri? = null
    private val viewModel: RegistrationClothViewModel by viewModels()

    private var isMaskingInProgress: Boolean = false

    private var mode: String = MODE_CREATE
    private var clothesId: Int = -1
    private var originalPhotoUrl: String? = null
    private var originalTags: ArrayList<Int> = arrayListOf()
    private var originalClothesType: Int? = null
    private var originalSeasons: ArrayList<Int> = arrayListOf()
    private var originalColor: String? = null
    private var isPhotoChanged: Boolean = false

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) selectedImageUri?.let { onPhotoSelected(it) }
        }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { onPhotoSelected(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraPermissionChecker.init(this)
        readArgs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireContext() as HomeActivity

        tagsSection = view.findViewById(R.id.section_tags)
        seasonSection = view.findViewById(R.id.section_season)
        clothTypeSection = view.findViewById(R.id.section_cloth_type)
        colorSection = view.findViewById(R.id.section_color)

        setupOptionSection()

        showPhotoPlaceholder("사진 등록")
        binding.btnRegistrationCloth.isEnabled = false

        if (mode == MODE_EDIT) {
            applyEditUi()
        } else {
            binding.btnRegistrationCloth.text = "등록"
        }

        binding.imbtnRegistrationCloth.setOnClickListener {
            if (isMaskingInProgress) {
                showToast("잠시 후 다시 시도해주세요.")
                return@setOnClickListener
            }

            AlertDialog.Builder(homeActivity)
                .setItems(arrayOf("카메라 촬영", "갤러리 선택")) { _, which ->
                    if (which == 0) openCamera() else openGalleryPicker()
                }
                .show()
        }

        binding.btnRegistrationCloth.setOnClickListener {
            val maskedUrl = viewModel.maskedImageUrl.value // ✅ 편집+사진안바꿈이면 null일 수 있음
            val tags = TagOptions.getSelectedTag(tagsSection)
            val clothesTypes = ClothTypeOptions.getClothType(clothTypeSection)
            val seasons = SeasonOptions.getSelectedSeason(seasonSection)
            val color = colorAdapter.getSelectedColor()

            val finalPhotoUrl =
                if (mode == MODE_EDIT && !isPhotoChanged) originalPhotoUrl else maskedUrl

            if (finalPhotoUrl.isNullOrBlank()) {
                showToast("사진이 필요 합니다.");
                return@setOnClickListener
            }
            if (tags.isEmpty()) {
                showToast("태그를 1개 이상 선택해주세요.");
                return@setOnClickListener
            }
            if (clothesTypes == null) {
                showToast("옷 종류를 선택해주세요.");
                return@setOnClickListener
            }
            if (seasons.isEmpty()) {
                showToast("계절을 선택해주세요.");
                return@setOnClickListener
            }
            if (color.isNullOrBlank()) {
                showToast("색상을 선택해주세요.");
                return@setOnClickListener
            }

            if (mode == MODE_EDIT) {
                viewModel.patchCloth(clothesId, finalPhotoUrl, tags, clothesTypes, seasons, color)
            } else {
                viewModel.registrationCloth(finalPhotoUrl, tags, clothesTypes, seasons, color)
            }
        }

        registerObserve()
    }

    private fun readArgs() {
        val args = arguments ?: return
        mode = args.getString(ARG_MODE, MODE_CREATE)
        clothesId = args.getInt(ARG_CLOTHES_ID, -1)
        originalPhotoUrl = args.getString(ARG_PHOTO_URL)
        originalTags = args.getIntegerArrayList(ARG_TAGS) ?: arrayListOf()
        originalClothesType = if (args.containsKey(ARG_CLOTHES_TYPE)) args.getInt(ARG_CLOTHES_TYPE) else null
        originalSeasons = args.getIntegerArrayList(ARG_SEASONS) ?: arrayListOf()
        originalColor = args.getString(ARG_COLOR)
    }

    private fun applyEditUi() {
        binding.btnRegistrationCloth.text = "수정"

        if (!originalPhotoUrl.isNullOrBlank()) {
            Glide.with(binding.imbtnRegistrationCloth).load(originalPhotoUrl).into(binding.imbtnRegistrationCloth)
            hidePhotoPlaceholder()
            binding.btnRegistrationCloth.isEnabled = true
        } else {
            showPhotoPlaceholder("사진 등록")
            binding.btnRegistrationCloth.isEnabled = false
        }

        TagOptions.setSelectedTag(tagsSection, originalTags)
        SeasonOptions.setSelectedSeason(seasonSection, originalSeasons)
        originalClothesType?.let { ClothTypeOptions.setClothType(clothTypeSection, it) }
        originalColor?.let { colorAdapter.setSelectedColor(it) }

        binding.tvTagsGuide.visibility = View.INVISIBLE
    }

    private fun onPhotoSelected(uri: Uri) {
        isPhotoChanged = true

        Glide.with(binding.imbtnRegistrationCloth).clear(binding.imbtnRegistrationCloth)
        binding.imbtnRegistrationCloth.setImageDrawable(null)

        isMaskingInProgress = true
        binding.btnRegistrationCloth.isEnabled = false

        viewModel.clearMaskedUrl()
        binding.tvTagsGuide.visibility = View.INVISIBLE
        showPhotoPlaceholder("배경 제거 중...")

        val clothesPhoto = ImageMultipartUtil.uriToCompressedMultipart(
            context = requireContext(),
            uri = uri,
            partName = "clothesPhoto",
            maxBytes = 600 * 1024,
            maxDimension = 1280
        )

        viewModel.removeImageBackground(clothesPhoto)
    }

    private fun setupOptionSection() {
        TagOptions.render(tagsSection, homeActivity)
        SeasonOptions.render(seasonSection, homeActivity)
        ClothTypeOptions.render(clothTypeSection, homeActivity)
        colorAdapter = ColorOptions.setup(colorSection)
    }

    private fun showPhotoPlaceholder(text: String) {
        binding.tvPhotoPlaceholder.text = text
        binding.tvPhotoPlaceholder.visibility = View.VISIBLE
        binding.tvTagsGuide.visibility = View.VISIBLE
    }

    private fun hidePhotoPlaceholder() {
        binding.tvPhotoPlaceholder.visibility = View.GONE
    }

    private fun openGalleryPicker() {
        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun openCamera() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (cameraPermissionChecker.checkPermission(homeActivity, permissions)) {
            launchCameraToUri()
        } else {
            cameraPermissionChecker.setOnGrantedListener { launchCameraToUri() }
            cameraPermissionChecker.requestPermissions(permissions)
        }
    }

    private fun launchCameraToUri() {
        selectedImageUri = createImageUri()
        selectedImageUri?.let { takePicture.launch(it) }
    }

    private fun createImageUri(): Uri? = try {
        val dir = File(requireContext().cacheDir, "images").apply { mkdirs() }
        val file = File(
            dir,
            "closetory_${ApplicationClass.sharedPreferences.getUserId(
                ApplicationClass.USERID
            )}_${System.currentTimeMillis()}.jpg"
        )
        FileProvider.getUriForFile(homeActivity, "${requireContext().packageName}.fileprovider", file)
    } catch (_: Exception) {
        null
    }

    @SuppressLint("CheckResult")
    private fun registerObserve() {
        // 마스킹 URL 수신
        viewModel.maskedImageUrl.observe(viewLifecycleOwner) { url ->
            if (url.isNullOrBlank()) return@observe
            Glide.with(binding.imbtnRegistrationCloth).load(url).into(binding.imbtnRegistrationCloth)
            isMaskingInProgress = false
            binding.btnRegistrationCloth.isEnabled = true
            hidePhotoPlaceholder()
        }

        // 토스트
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.message.collect { msg ->
                if (!msg.isNullOrBlank()) showToast(msg)
            }
        }

        // 등록/수정 성공 시 상세 화면으로 이동
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigateToDetail.collect { id ->
                navigateToClothesDetail(id)
            }
        }
    }

    private fun navigateToClothesDetail(clothesId: Int) {
        val detail = ClothesDetailFragment().apply {
            arguments = Bundle().apply { putInt("clothesId", clothesId) }
        }

        // TODO: 아래 container id는 너희 HomeActivity에서 fragment 붙이는 id로 교체
        homeActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.container, detail)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        private const val ARG_MODE = "mode"
        private const val ARG_CLOTHES_ID = "clothesId"
        private const val ARG_PHOTO_URL = "photoUrl"
        private const val ARG_TAGS = "tags"
        private const val ARG_CLOTHES_TYPE = "clothesType"
        private const val ARG_SEASONS = "seasons"
        private const val ARG_COLOR = "color"

        const val MODE_CREATE = "create"
        const val MODE_EDIT = "edit"
    }
}
