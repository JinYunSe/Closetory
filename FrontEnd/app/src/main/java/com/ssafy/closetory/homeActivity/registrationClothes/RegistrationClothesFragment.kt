package com.ssafy.closetory.homeActivity.registrationClothes

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
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
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentRegistrationClothesBinding
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.util.ClothTypeOptions
import com.ssafy.closetory.util.ColorOptions
import com.ssafy.closetory.util.OptionItem
import com.ssafy.closetory.util.PermissionChecker
import com.ssafy.closetory.util.SeasonOptions
import com.ssafy.closetory.util.TagOptions
import com.ssafy.closetory.util.image.ImageMultipartUtil
import java.io.File
import kotlinx.coroutines.launch

private const val TAG = "RegistrationClothesFragment_싸피"

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
    private var autoSelectApplied: Boolean = false
    private var lastAutoSelectUrl: String? = null
    private var pendingTagCodes: List<Int> = emptyList()

    private val imageLabeler by lazy {
        ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    }

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
        autoSelectApplied = false
        lastAutoSelectUrl = null

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

        if (pendingTagCodes.isNotEmpty() && TagOptions.isReady()) {
            TagOptions.setSelectedTag(tagsSection, pendingTagCodes)
            pendingTagCodes = emptyList()
        }
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
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        isMaskingInProgress = false
                        binding.btnRegistrationClothes.isEnabled = false
                        showPhotoPlaceholder("사진 등록")
                        showToast("이미지 로드에 실패했습니다.")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        isMaskingInProgress = false
                        binding.btnRegistrationClothes.isEnabled = true
                        hidePhotoPlaceholder()

                        if (shouldAutoSelect(url)) {
                            autoSelectFromImageUrl(url)
                        }
                        return false
                    }
                })
                .into(binding.imbtnRegistrationClothes)
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

    private fun shouldAutoSelect(url: String): Boolean {
        if (mode == MODE_EDIT && !isPhotoChanged) return false
        if (autoSelectApplied && lastAutoSelectUrl == url) return false
        return true
    }

    private fun autoSelectFromImageUrl(url: String) {
        autoSelectApplied = true
        lastAutoSelectUrl = url

        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    runAutoSelect(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) = Unit
            })
    }

    private fun runAutoSelect(bitmap: Bitmap) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        imageLabeler.process(inputImage)
            .addOnSuccessListener { labels ->
                val clothType = mapLabelsToClothType(labels)
                val seasons = mapLabelsToSeasons(labels)
                val color = detectDominantColorCode(bitmap)
                val tagCodes = mapLabelsToTags(labels, TagOptions.items)

                applyAutoSelectResult(
                    clothType = clothType,
                    seasons = seasons,
                    color = color,
                    tagCodes = tagCodes
                )
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "auto select failed: ${e.message}")
            }
    }

    private fun applyAutoSelectResult(clothType: String?, seasons: List<Int>, color: String?, tagCodes: List<Int>) {
        Log.d(
            TAG,
            "autoSelect result: clothType=$clothType seasons=$seasons color=$color tagCodes=$tagCodes ready=${TagOptions.isReady()}"
        )
        clothType?.let { ClothTypeOptions.setClothTypeByEnglish(clothTypeSection, it) }
        if (seasons.isNotEmpty()) SeasonOptions.setSelectedSeason(seasonSection, seasons)
        color?.let { colorAdapter.setSelectedColor(it) }
        if (tagCodes.isNotEmpty()) {
            if (TagOptions.isReady()) {
                TagOptions.setSelectedTag(tagsSection, tagCodes)
            } else {
                pendingTagCodes = tagCodes
            }
        }
    }

    private fun mapLabelsToClothType(labels: List<ImageLabel>): String? {
        val text = labels.joinToString(" ") { it.text.lowercase() }
        return when {
            listOf("shoe", "sneaker", "boot", "heel", "sandal").any { text.contains(it) } -> "SHOES"
            listOf("bag", "backpack", "handbag", "tote").any { text.contains(it) } -> "BAG"
            listOf("hat", "cap", "beanie", "scarf", "belt", "accessory").any { text.contains(it) } -> "ACCESSORY"
            listOf("coat", "jacket", "outerwear", "parka", "cardigan", "hoodie").any { text.contains(it) } -> "OUTER"
            listOf("pants", "jeans", "trousers", "skirt", "shorts").any { text.contains(it) } -> "BOTTOM"
            listOf("shirt", "t-shirt", "tee", "blouse", "top", "sweater").any { text.contains(it) } -> "TOP"
            else -> null
        }
    }

    private fun mapLabelsToSeasons(labels: List<ImageLabel>): List<Int> {
        val text = labels.joinToString(" ") { it.text.lowercase() }
        val seasons = mutableSetOf<Int>()
        if (listOf("coat", "jacket", "parka", "sweater", "hoodie", "wool").any { text.contains(it) }) {
            seasons.add(SeasonOptions.toCode("WINTER") ?: 4)
            seasons.add(SeasonOptions.toCode("FALL") ?: 3)
        }
        if (listOf("t-shirt", "tee", "shorts", "sleeveless", "tank", "swim").any { text.contains(it) }) {
            seasons.add(SeasonOptions.toCode("SUMMER") ?: 2)
        }
        if (seasons.isEmpty()) {
            seasons.add(SeasonOptions.toCode("SPRING") ?: 1)
            seasons.add(SeasonOptions.toCode("FALL") ?: 3)
        }
        return seasons.toList()
    }

    private fun mapLabelsToTags(labels: List<ImageLabel>, items: List<OptionItem>): List<Int> {
        if (items.isEmpty()) return emptyList()

        val labelText = labels.joinToString(" ") { it.text.lowercase() }
        Log.d(TAG, "tag labelText=$labelText items=${items.map { it.codeKorean }}")
        val tagKeywordMap = mapOf(
            "일상" to listOf("daily", "everyday", "casual"),
            "캐주얼" to listOf("casual", "relaxed"),
            "출근" to listOf("office", "business", "formal", "suit"),
            "트렌디" to listOf("trendy", "fashion", "stylish"),
            "밝음" to listOf("bright", "light", "colorful"),
            "남성스러움" to listOf("male", "man", "masculine"),
            "여성스러움" to listOf("female", "woman", "feminine", "dress", "skirt"),
            "데이트" to listOf("date", "romantic"),
            "운동" to listOf("sport", "sports", "athletic", "running", "fitness", "gym"),
            "시크" to listOf("chic", "sleek", "minimal"),
            "빈티지" to listOf("vintage", "retro"),
            "격식있는" to listOf("formal", "suit", "business"),
            "유니크" to listOf("unique", "quirky", "distinct"),
            "귀여움" to listOf("cute", "kawaii", "adorable"),
            "여행" to listOf("travel", "vacation", "outdoor"),
            "화려함" to listOf("glam", "glamorous", "flashy", "luxury")
        )

        val matches = items.filter { item ->
            val keywords = tagKeywordMap[item.codeKorean.trim()] ?: return@filter false
            keywords.any { key -> labelText.contains(key) }
        }.map { it.code }.distinct().toMutableList()

        // 기본 의류 라벨에 대한 보정 매핑
        if (matches.isEmpty()) {
            val fallbackMap = mapOf(
                "캐주얼" to listOf("denim", "jeans", "shorts", "t-shirt", "tee"),
                "일상" to listOf("jacket", "shirt", "top", "pants"),
                "시크" to listOf("jacket", "coat", "leather"),
                "운동" to listOf("shorts", "sports", "athletic", "running", "gym")
            )

            fallbackMap.forEach { (tagName, keys) ->
                if (keys.any { key -> labelText.contains(key) }) {
                    items.firstOrNull { it.codeKorean == tagName }?.let { matches.add(it.code) }
                }
            }
        }

        return matches.distinct()
    }

    private fun detectDominantColorCode(bitmap: Bitmap): String? {
        val scaled = Bitmap.createScaledBitmap(bitmap, 64, 64, true)
        var sumR = 0L
        var sumG = 0L
        var sumB = 0L
        var count = 0L

        for (y in 0 until scaled.height) {
            for (x in 0 until scaled.width) {
                val pixel = scaled.getPixel(x, y)
                val alpha = Color.alpha(pixel)
                if (alpha < 128) continue
                sumR += Color.red(pixel)
                sumG += Color.green(pixel)
                sumB += Color.blue(pixel)
                count++
            }
        }
        if (count == 0L) return null

        val avgR = (sumR / count).toInt()
        val avgG = (sumG / count).toInt()
        val avgB = (sumB / count).toInt()

        val candidates = ColorOptions.items.filter { it.codeEnglish != "OTHER" }
        var best: String? = null
        var bestDist = Int.MAX_VALUE

        for (c in candidates) {
            val cr = Color.red(c.argb)
            val cg = Color.green(c.argb)
            val cb = Color.blue(c.argb)
            val dist = (avgR - cr) * (avgR - cr) + (avgG - cg) * (avgG - cg) + (avgB - cb) * (avgB - cb)
            if (dist < bestDist) {
                bestDist = dist
                best = c.codeEnglish
            }
        }
        return best
    }
}
