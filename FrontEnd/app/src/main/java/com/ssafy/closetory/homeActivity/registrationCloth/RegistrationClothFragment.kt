package com.ssafy.closetory.homeActivity.registrationCloth

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentRegistrationClothBinding
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.util.ClothTypeOptions
import com.ssafy.closetory.util.ColorOptions
import com.ssafy.closetory.util.PermissionChecker
import com.ssafy.closetory.util.SeasonOptions
import com.ssafy.closetory.util.TagOptions
import com.ssafy.closetory.util.image.ImageUtil
import com.ssafy.closetory.util.image.SegmentationDialog
import java.io.File

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

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) selectedImageUri?.let { onImageSelected(it) }
        }

    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { onImageSelected(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraPermissionChecker.init(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireActivity() as HomeActivity

        tagsSection = view.findViewById(R.id.section_tags)
        seasonSection = view.findViewById(R.id.section_season)
        clothTypeSection = view.findViewById(R.id.section_cloth_type)
        colorSection = view.findViewById(R.id.section_color)

        setupOptionSection()
        showPhotoPlaceholder(selectedImageUri == null)

        binding.imbtnRegistrationCloth.setOnClickListener { showPickDialog() }
        binding.btnRegistrationCloth.setOnClickListener { submit() }
    }

    private fun showPickDialog() {
        AlertDialog.Builder(homeActivity)
            .setTitle("사진 가져오기")
            .setItems(arrayOf("카메라 촬영", "갤러리 선택")) { _, which ->
                when (which) {
                    0 -> ensureCameraPermissionThenLaunch()
                    1 -> launchPhotoPicker()
                }
            }.show()
    }

    private fun onImageSelected(uri: Uri) {
        selectedImageUri = uri

        val bitmap = ImageUtil.uriToBitmap(requireContext(), uri)
            ?.let { ImageUtil.downscaleIfNeeded(it, 1024) }
            ?: return

        binding.imbtnRegistrationCloth.setImageBitmap(bitmap)
        showPhotoPlaceholder(false)

        SegmentationDialog.show(
            fragment = this,
            sourceBitmap = bitmap
        ) { cutout ->
            val newUri = ImageUtil.saveBitmapToCache(requireContext(), cutout)
            selectedImageUri = newUri
            binding.imbtnRegistrationCloth.setImageBitmap(cutout)
        }
    }

    private fun submit() {
        val photoUri = selectedImageUri ?: return showToast("사진을 등록해주세요.")

        val tags = TagOptions.getSelectedTag(tagsSection)
        val clothType = ClothTypeOptions.getClothType(clothTypeSection)
        val seasons = SeasonOptions.getSelectedSeason(seasonSection)
        val color = colorAdapter.getSelectedColor()

        if (tags.isEmpty() || clothType == null || seasons.isEmpty() || color.isNullOrBlank()) {
            showToast("모든 항목을 입력해주세요.")
            return
        }

        viewModel.registrationCloth(photoUri, tags, clothType, seasons, color)
    }

    private fun setupOptionSection() {
        TagOptions.render(tagsSection, homeActivity)
        SeasonOptions.render(seasonSection, homeActivity)
        ClothTypeOptions.render(clothTypeSection, homeActivity)
        colorAdapter = ColorOptions.setup(colorSection)
    }

    private fun showPhotoPlaceholder(show: Boolean) {
        binding.tvPhotoPlaceholder.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun launchPhotoPicker() {
        pickPhotoLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun ensureCameraPermissionThenLaunch() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (cameraPermissionChecker.checkPermission(homeActivity, permissions)) {
            launchCamera()
            return
        }
        cameraPermissionChecker.setOnGrantedListener { launchCamera() }
        cameraPermissionChecker.requestPermissions(permissions)
    }

    private fun launchCamera() {
        val uri = createImageUri()
        selectedImageUri = uri
        uri?.let { takePictureLauncher.launch(it) }
    }

    private fun createImageUri(): Uri? = try {
        val dir = File(requireContext().cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "camera_${System.currentTimeMillis()}.png")
        FileProvider.getUriForFile(
            requireContext(),
            "${homeActivity.packageName}.fileprovider",
            file
        )
    } catch (_: Exception) {
        null
    }
}
