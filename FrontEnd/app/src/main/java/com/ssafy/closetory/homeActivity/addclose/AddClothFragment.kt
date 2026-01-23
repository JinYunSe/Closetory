package com.ssafy.closetory.homeActivity.addClose

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.ChipGroup
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentAddClothBinding
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.util.ClothTypeOptions
import com.ssafy.closetory.util.ColorOptions
import com.ssafy.closetory.util.PermissionChecker
import com.ssafy.closetory.util.SeasonOptions
import com.ssafy.closetory.util.TagOptions

class AddClothFragment :
    BaseFragment<FragmentAddClothBinding>(FragmentAddClothBinding::bind, R.layout.fragment_add_cloth) {

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private var cameraImageUri: android.net.Uri? = null

    private val takePicture =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val uri = cameraImageUri ?: return@registerForActivityResult
                // TODO: uri로 이미지 표시/업로드 처리
                binding.imbtnRegistrationCloth.setImageURI(uri)
            }
        }

    private val pickImage =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                // TODO: uri로 이미지 표시/업로드 처리
                binding.imbtnRegistrationCloth.setImageURI(uri)
            }
        }

    private lateinit var homeActivity: HomeActivity

    private lateinit var colorAdapter: ColorOptions.ColorAdapter

    private lateinit var tagsSection: View

    private lateinit var seasonSection: View

    private lateinit var clothTypeSection: View

    private lateinit var colorSection: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireContext() as HomeActivity

        // 다른 XML 레이어 파일 가져오기
        tagsSection = view.findViewById<View>(R.id.section_tags)
        seasonSection = view.findViewById<View>(R.id.section_season)
        clothTypeSection = view.findViewById<View>(R.id.section_cloth_type)
        colorSection = view.findViewById<View>(R.id.section_color)

        init()

        binding.imbtnRegistrationCloth.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(homeActivity)
                .setTitle("사진 가져오기")
                .setItems(arrayOf("카메라 촬용", "갤러리 선택")) { _, which ->
                    when (which) {
                        0 -> openCamera()
                        1 -> openGalleryPicker()
                    }
                }.show()
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val photo = result.data?.extras?.get("data") as? Bitmap
                photo?.let {
                    binding.imbtnRegistrationCloth.setImageBitmap(it)
                }
            }
        }
    }

    fun init() {
        TagOptions.render(tagsSection, homeActivity)
        SeasonOptions.render(seasonSection, homeActivity)
        ClothTypeOptions.render(clothTypeSection, homeActivity)
        colorAdapter = ColorOptions.setup(colorSection)
    }

    private fun openGalleryPicker() {
        pickImage.launch(
            androidx.activity.result.PickVisualMediaRequest(
                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    private fun openCamera() {
        val permissionChecker = PermissionChecker(this)
        val permissions = arrayOf(android.Manifest.permission.CAMERA)

        if (permissionChecker.checkPermission(homeActivity, permissions)) {
            launchCamera()
        } else {
            permissionChecker.setOnGrantedListener {
                launchCamera()
            }
            permissionChecker.requestPermissionLauncher.launch(permissions)
        }
    }

    private fun launchCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }
}
