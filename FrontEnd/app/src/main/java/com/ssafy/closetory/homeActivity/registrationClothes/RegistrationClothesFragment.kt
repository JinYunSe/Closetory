package com.ssafy.closetory.homeActivity.registrationClothes

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentRegistrationClothesBinding
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.util.ClothTypeOptions
import com.ssafy.closetory.util.ColorOptions
import com.ssafy.closetory.util.PermissionChecker
import com.ssafy.closetory.util.SeasonOptions
import com.ssafy.closetory.util.TagOptions
import com.ssafy.closetory.util.image.ImageMultipartUtil
import java.io.File
import kotlinx.coroutines.launch

private const val TAG = "RegistrationClothesFragment"

class RegistrationClothesFragment :
    BaseFragment<FragmentRegistrationClothesBinding>(
        FragmentRegistrationClothesBinding::bind,
        R.layout.fragment_registration_clothes
    ) {

    private lateinit var homeActivity: HomeActivity

    private val cameraPermissionChecker = PermissionChecker()

    private lateinit var colorAdapter: ColorOptions.ColorAdapter
    private lateinit var tagsSection: View
    private lateinit var seasonSection: View
    private lateinit var clothTypeSection: View
    private lateinit var colorSection: View

    private var selectedImageUri: Uri? = null
    private val viewModel: RegistrationClothesViewModel by viewModels()

    private var isMaskingInProgress: Boolean = false

    private var mode: String = MODE_CREATE
    private var clothesId: Int = -1
    private var originalPhotoUrl: String? = null
    private var originalTags: ArrayList<Int> = arrayListOf()
    private var originalClothesType: String? = null
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

        homeActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.navigation_closet, false)
        }

        tagsSection = view.findViewById(R.id.section_tags)
        seasonSection = view.findViewById(R.id.section_season)
        clothTypeSection = view.findViewById(R.id.section_clothes_type)
        colorSection = view.findViewById(R.id.section_color)

        setupOptionSection()

        binding.btnPhotoGuide.setOnClickListener {
            AlertDialog.Builder(homeActivity)
                .setTitle("촬영 도움말")
                .setMessage("옷이 잘 나오도록 주변을 정리한 뒤 촬영해주세요.")
                .setPositiveButton("확인", null)
                .show()
        }

        showPhotoPlaceholder("사진 등록")
        binding.btnRegistrationClothes.isEnabled = false

        if (mode == MODE_EDIT) {
            applyEditUi()
        } else {
            binding.btnRegistrationClothes.text = "등록"
        }

        binding.imbtnRegistrationClothes.setOnClickListener {
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

        binding.btnRegistrationClothes.setOnClickListener {
            val maskedUrl = viewModel.maskedImageUrl.value
            val tags = TagOptions.getSelectedTag(tagsSection)
            val clothesType = ClothTypeOptions.getClothTypeEnglish(clothTypeSection)
            val seasons = SeasonOptions.getSelectedSeason(seasonSection)
            val color = colorAdapter.getSelectedColor()

            val finalPhotoUrl =
                if (mode == MODE_EDIT && !isPhotoChanged) originalPhotoUrl else maskedUrl

            if (finalPhotoUrl.isNullOrBlank()) {
                showToast("사진이 필요합니다.")
                return@setOnClickListener
            }
            if (tags.isEmpty()) {
                showToast("태그를 1개 이상 선택해주세요.")
                return@setOnClickListener
            }
            if (clothesType == null) {
                showToast("옷 종류를 선택해주세요.")
                return@setOnClickListener
            }
            if (seasons.isEmpty()) {
                showToast("계절을 선택해주세요.")
                return@setOnClickListener
            }
            if (color.isNullOrBlank()) {
                showToast("색상을 선택해주세요.")
                return@setOnClickListener
            }

            if (mode == MODE_EDIT) {
                Log.d(TAG, "수정 동작")
                viewModel.patchCloth(clothesId, finalPhotoUrl, tags, clothesType, seasons, color)
            } else {
                Log.d(TAG, "등록 동작")
                viewModel.registrationCloth(finalPhotoUrl, tags, clothesType, seasons, color)
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
        originalClothesType = args.getString(ARG_CLOTHES_TYPE)
        originalSeasons = args.getIntegerArrayList(ARG_SEASONS) ?: arrayListOf()
        originalColor = args.getString(ARG_COLOR)
    }

    private fun applyEditUi() {
        binding.btnRegistrationClothes.text = "수정"

        if (!originalPhotoUrl.isNullOrBlank()) {
            Glide.with(binding.imbtnRegistrationClothes)
                .load(originalPhotoUrl)
                .into(binding.imbtnRegistrationClothes)

            hidePhotoPlaceholder()
            binding.btnRegistrationClothes.isEnabled = true
        } else {
            showPhotoPlaceholder("사진 등록")
            binding.btnRegistrationClothes.isEnabled = false
        }

        TagOptions.setSelectedTag(tagsSection, originalTags)
        SeasonOptions.setSelectedSeason(seasonSection, originalSeasons)
        originalClothesType?.let { ClothTypeOptions.setClothTypeByEnglish(clothTypeSection, it) }
        originalColor?.let { colorAdapter.setSelectedColor(it) }
    }

    private fun onPhotoSelected(uri: Uri) {
        isPhotoChanged = true

        Glide.with(binding.imbtnRegistrationClothes).clear(binding.imbtnRegistrationClothes)
        binding.imbtnRegistrationClothes.setImageDrawable(null)

        isMaskingInProgress = true
        binding.btnRegistrationClothes.isEnabled = false

        viewModel.clearMaskedUrl()

        showPhotoPlaceholder("배경 제거 중...")

        val clothesPhoto = ImageMultipartUtil.uriToCompressedMultipart(
            context = homeActivity,
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
        val dir = File(homeActivity.cacheDir, "images").apply { mkdirs() }
        val file = File(
            dir,
            "closetory_${ApplicationClass.sharedPreferences.getUserId(
                ApplicationClass.USERID
            )}_${System.currentTimeMillis()}.jpg"
        )
        FileProvider.getUriForFile(
            homeActivity,
            "${homeActivity.packageName}.fileprovider",
            file
        )
    } catch (_: Exception) {
        null
    }

    @SuppressLint("CheckResult")
    private fun registerObserve() {
        viewModel.maskedImageUrl.observe(viewLifecycleOwner) { url ->
            if (url.isNullOrBlank()) return@observe

            Glide.with(binding.imbtnRegistrationClothes)
                .load(url)
                .into(binding.imbtnRegistrationClothes)

            isMaskingInProgress = false
            binding.btnRegistrationClothes.isEnabled = true
            hidePhotoPlaceholder()

        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.message.collect { msg ->
                if (!msg.isNullOrBlank()) showToast(msg)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigateToDetail.collect { id ->
                navigateToClothesDetail(id)
            }
        }
    }

    private fun navigateToClothesDetail(clothesId: Int) {
        val bundle = Bundle().apply {
            putInt("clothesId", clothesId)
        }

        findNavController().navigate(
            R.id.action_registration_to_clothes_detail,
            bundle
        )
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
