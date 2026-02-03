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
import com.ssafy.closetory.util.ui.BalloonTooltip
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

    private var photoGuideTooltip: BalloonTooltip? = null

    // ✅ 새 사진 요청 구분용(늦게 온 maskedUrl 응답 무시)
    private var photoRequestToken: Long = 0L

    /**
     * 라벨 더 많이 받고 싶으면 threshold 낮추기(노이즈 증가).
     * 필요 없으면 DEFAULT_OPTIONS로 돌려도 됨.
     */
    private val imageLabeler by lazy {
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.35f)
            .build()
        ImageLabeling.getClient(options)
    }

    // -----------------------------
    // [중복 금지] 키워드 사전(태그/옷종류/계절)
    // -----------------------------

    // 태그 키워드 대폭 확장 + (태그 간 키워드 중복 금지)
    private val tagKeywordMap: Map<String, Set<String>> = mapOf(
        "일상" to setOf(
            "daily", "everyday", "daywear", "routine", "regular", "basiclook",
            "basic", "simple", "plain", "minimalday", "daytime", "weekday",
            "dailymood", "wearable", "easylook", "easywear", "normalwear",
            "simplefit", "cleanbasic", "dayoutfit", "everydaylook", "basicfit"
        ),
        "캐주얼" to setOf(
            "casual", "relaxed", "streetwear", "denim", "hoodiestyle", "jeanswear",
            "laidback", "comfy", "comfortwear", "weekendwear", "streetstyle",
            "sweatshirt", "sweatpants", "oversize", "loosefit", "campuslook",
            "skater", "hiphopstyle", "urbanlook", "normcore",
            "casuallook", "streetfit", "casualfit", "easycasual", "relaxedfit"
        ),
        "출근" to setOf(
            "office", "workwear", "business", "smartwear", "blazerwear", "slackswear",
            "corporate", "commute", "commuter", "meetinglook", "presentation",
            "worklook", "smartcasual", "businesscasual", "formalwork",
            "dresscode", "neatlook", "cleanoffice", "weekdaywork",
            "workfit", "officefit", "commutefit", "neatwork", "smartwork"
        ),
        "트렌디" to setOf(
            "trendy", "fashion", "stylish", "newlook", "statement", "lookbook",
            "trend", "hype", "itlook", "musthave", "seasontrend", "runway",
            "streettrend", "fashionable", "styleicon", "viral", "hotitem",
            "editorpick", "ootd", "stylegram", "lookoftheday",
            "trendfit", "hypefit", "fashionfit", "newtrend", "ititem"
        ),
        "밝음" to setOf(
            "bright", "pastel", "colorful", "vivid", "cheerful", "lighttone",
            "fresh", "sunny", "springtone", "softcolor", "highkey", "lightcolor",
            "lively", "happyvibe", "buttery", "cream", "ivorytone", "whitetone",
            "lightmood", "freshfit", "brightfit", "sunnyfit", "lightvibe"
        ),
        "남성스러움" to setOf(
            "masculine", "menswear", "tailoredman", "rugged", "manstyle",
            "manly", "stronglook", "military", "utility", "workman",
            "classicman", "gentstyle", "heritageman", "rough", "boyish",
            "dapperman", "sharpman", "powerlook",
            "mensfit", "manfit", "strongfit", "utilfit", "militaryfit"
        ),
        "여성스러움" to setOf(
            "feminine", "womanstyle", "floral", "lace", "girlish", "dressy",
            "softfemme", "elegantfemme", "ladylook", "romancefemme",
            "ribbon", "frill", "pleats", "chiffon", "silky",
            "bloom", "flowerprint", "pastelfemme", "delicate",
            "femmefit", "ladyfit", "softfit", "girlyfit", "elegantfit"
        ),
        "데이트" to setOf(
            "romantic", "lovely", "nightout", "dinnerdate", "charming",
            "datenight", "couplelook", "sweetdate", "romance", "heartvibe",
            "prettylook", "specialday", "anniversary", "valentine",
            "cozydate", "softromance", "dately", "flirty",
            "datefit", "romancefit", "lovelyfit", "dinnerfit", "nightfit"
        ),
        "운동" to setOf(
            "athletic", "activewear", "gym", "workout", "training", "sportswear", "running",
            "fitness", "yoga", "pilates", "tennis", "golf", "soccer", "basketball",
            "cycling", "hikinggear", "outdooractive", "track", "performancewear",
            "stretchwear", "sweatproof", "dryfit",
            "sportfit", "gymfit", "runfit", "trainfit", "activefit"
        ),
        "시크" to setOf(
            "chic", "minimal", "sleek", "monochrome", "allblack", "cleanlook",
            "sharp", "modernminimal", "cooltone", "blacklook", "neutral",
            "simplechic", "cleanfit", "citylook", "urbanminimal",
            "edgy", "coldvibe", "noir", "structured",
            "chicfit", "noirfit", "sleekfit", "monofit", "minimalfit"
        ),
        "빈티지" to setOf(
            "vintage", "retro", "heritage", "oldschool", "classicvibe",
            "throwback", "nostalgia", "antique", "secondhand", "usedlook",
            "oldmoney", "classicretro", "heritagestyle", "archive",
            "americana", "denimretro", "y2k", "seventies", "nineties",
            "vintagefit", "retrofit", "archivefit", "classicfit", "oldschoolfit"
        ),
        "격식있는" to setOf(
            "formal", "ceremony", "tuxedo", "dressshirt", "blacktie",
            "eveningwear", "gala", "weddingguest", "suitup", "formalwear",
            "luxformal", "classicformal", "banquet", "eventlook",
            "ceremonial", "proper", "dresscodeformal", "smartformal",
            "formalfit", "suitlook", "properfit", "eventformal", "galafit"
        ),
        "유니크" to setOf(
            "unique", "quirky", "distinct", "unusual", "graphic", "patterned",
            "boldprint", "artsy", "weirdcore", "statementpiece", "experimental",
            "avantgarde", "eccentric", "funky", "colorblock", "mixedpattern",
            "uncommon", "signaturelook", "oneofakind",
            "uniquefit", "quirkyfit", "artsyfit", "funkyfit", "oddlook"
        ),
        "귀여움" to setOf(
            "cute", "kawaii", "adorable", "sweet", "babydoll",
            "lovelycute", "softcute", "pastelcute", "girlycute", "dollcore",
            "playful", "charmingcute", "prettycute", "cutie", "cutesy",
            "tiny", "puffy", "heartprint", "cartoon",
            "cutefit", "kawaiifit", "sweetfit", "dollfit", "playfulfit"
        ),
        "여행" to setOf(
            "travel", "vacation", "holiday", "resort", "camping", "hiking",
            "trip", "tour", "airportlook", "travelwear", "getaway",
            "beachwear", "summertrip", "outdoortrip", "backpacking",
            "roadtrip", "citytour", "mountaintrip", "islandtrip",
            "travelfit", "airportfit", "beachfit", "campfit", "hikefit"
        ),
        "화려함" to setOf(
            "glam", "luxury", "sparkle", "shiny", "partywear", "flashy",
            "glitter", "sequins", "metallic", "bling", "shine",
            "nightglam", "redcarpet", "luxvibe", "boldglam",
            "fancy", "showy", "statementglam", "spotlight",
            "glamfit", "partyfit", "blingfit", "sparklefit", "flashyfit"
        )
    )

    /**
     * 옷 종류 간 키워드 "절대 중복 금지" 버전
     * - hoodie/hoodies는 TOP으로 분류되도록 TOP에만 둠
     */
    private val clothTypeKeywords: Map<String, Set<String>> = mapOf(
        "SHOES" to setOf(
            "shoe", "shoes", "sneaker", "sneakers", "boot", "boots", "heel", "heels",
            "sandal", "sandals", "loafer", "loafers", "flat", "flats"
        ),
        "BAG" to setOf(
            "bag", "bags", "backpack", "backpacks", "handbag", "handbags",
            "tote", "totes", "crossbody", "clutch", "pouch"
        ),
        "ACCESSORY" to setOf(
            "accessory", "accessories", "hat", "hats", "cap", "caps", "beanie",
            "scarf", "scarves", "belt", "belts", "glasses", "sunglasses", "tie", "watch"
        ),
        "OUTER" to setOf(
            "outerwear", "coat", "coats", "jacket", "jackets", "parka", "parkas",
            "blazer", "blazers", "trench", "windbreaker", "puffer", "bomber"
        ),
        "BOTTOM" to setOf(
            "pants", "pant", "trouser", "trousers", "jean", "jeans", "denim",
            "skirt", "skirts", "short", "shorts", "legging", "leggings",
            "slacks", "jogger", "joggers"
        ),
        "TOP" to setOf(
            "shirt", "shirts", "tshirt", "tee", "tees", "blouse", "blouses",
            "sweater", "sweaters", "knit", "tank", "tanktop", "sleeveless", "polo",
            "hoodie", "hoodies"
        )
    )

    /**
     * 계절 간 키워드 "절대 중복 금지" 버전
     * - WINTER / SUMMER만 키워드 기반 추론
     * - FALL/SPRING은 fallback으로만 추가(키워드 겹침 문제 제거)
     */
    private val seasonKeywords: Map<String, Set<String>> = mapOf(
        "WINTER" to setOf(
            "wool",
            "cashmere",
            "fleece",
            "down",
            "puffer",
            "thermal",
            "heavycoat"
        ),
        "SUMMER" to setOf(
            "linen",
            "sleeveless",
            "tank",
            "shorts",
            "swimwear",
            "lightfabric"
        )
    )

    /**
     * 개발 중에 중복 키워드가 들어오면 즉시 잡기 위한 검증.
     * - “태그/옷종류/계절 내부 요소 중복 금지” 강제
     */
    private fun validateNoKeywordOverlap(map: Map<String, Set<String>>, mapName: String) {
        val used = mutableMapOf<String, String>() // keyword -> ownerKey
        val duplicates = mutableListOf<String>()

        map.forEach { (owner, keys) ->
            keys.forEach { kw ->
                val prev = used.putIfAbsent(kw, owner)
                if (prev != null && prev != owner) {
                    duplicates.add("$kw ($prev vs $owner)")
                }
            }
        }

        if (duplicates.isNotEmpty()) {
            Log.e(TAG, "[$mapName] keyword overlap found: ${duplicates.joinToString()}")
            throw IllegalStateException("[$mapName] keyword overlap found: ${duplicates.joinToString()}")
        }
    }

    // -----------------------------

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

        // [중복 금지] 사전 검증(겹치면 즉시 크래시로 잡아냄)
        validateNoKeywordOverlap(tagKeywordMap, "tagKeywordMap")
        validateNoKeywordOverlap(clothTypeKeywords, "clothTypeKeywords")
        validateNoKeywordOverlap(seasonKeywords, "seasonKeywords")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireContext() as HomeActivity
        photoGuideTooltip = BalloonTooltip(homeActivity)

        homeActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.navigation_closet, false)
        }

        tagsSection = view.findViewById(R.id.section_tags)
        seasonSection = view.findViewById(R.id.section_season)
        clothTypeSection = view.findViewById(R.id.section_clothes_type)
        colorSection = view.findViewById(R.id.section_color)

        setupOptionSection()

        binding.btnPhotoGuide.setOnClickListener { v ->
            photoGuideTooltip?.show(
                anchor = v,
                message = """
                    옷 전체가 잘 보이도록 촬영해주세요.
                    배경에 다른 물건이 없을수록 인식이 정확해요.
                    옷을 정돈한 뒤 촬영해 주세요.
                """.trimIndent(),
                autoDismissMs = 2500
            )
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

            android.app.AlertDialog.Builder(homeActivity)
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

    override fun onDestroyView() {
        photoGuideTooltip?.dismiss()
        photoGuideTooltip = null
        super.onDestroyView()
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

    // 사진 선택/촬영 → 배경 제거 요청 시작
    private fun onPhotoSelected(uri: Uri) {
        isPhotoChanged = true
        autoSelectApplied = false
        lastAutoSelectUrl = null

        // 새 요청 토큰(늦게 온 응답 무시)
        photoRequestToken = System.currentTimeMillis()

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

    // 배경 제거된 이미지가 들어오면, 그 이미지 기준으로 태그/옷종류/계절/색상을 “다시 선택”해야 하므로
    // 선택 UI를 통째로 리셋(render/setup)하는 방식으로 초기화한다.
    private fun resetSelectionsUi() {
        TagOptions.render(tagsSection, homeActivity)
        SeasonOptions.render(seasonSection, homeActivity)
        ClothTypeOptions.render(clothTypeSection, homeActivity)
        colorAdapter = ColorOptions.setup(colorSection)
        pendingTagCodes = emptyList()
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

            // 이 observe 실행 시점의 토큰 캡처
            val tokenAtRequest = photoRequestToken

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
                        // ✅ 새 사진이 선택된 뒤에 늦게 온 응답이면 무시
                        if (tokenAtRequest != photoRequestToken) return false

                        isMaskingInProgress = false
                        binding.btnRegistrationClothes.isEnabled = true
                        hidePhotoPlaceholder()

                        // ✅ 배경 제거된 이미지가 들어올 때마다
                        //    "그 이미지 기준으로" 태그/옷종류/계절/색상을 다시 선택
                        resetSelectionsUi()

                        // ✅ URL 기준으로 무조건 autoSelect 수행
                        autoSelectApplied = false
                        lastAutoSelectUrl = null
                        autoSelectFromImageUrl(url)

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
        // 배경 제거 이미지가 들어올 때마다 다시 선택해야 하므로,
        // 여기서는 중복 방지 로직을 사실상 무력화해도 됨.
        // (하지만 같은 URL이 연속으로 들어오는 케이스는 막아둠)
        if (autoSelectApplied && lastAutoSelectUrl == url) return false
        return true
    }

    private fun autoSelectFromImageUrl(url: String) {
        if (!shouldAutoSelect(url)) return

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
                // 라벨 확인 로그
                Log.d(TAG, "labels=" + labels.joinToString { "${it.text}:${"%.2f".format(it.confidence)}" })

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

    // 결과가 비어도 “이전 값 유지” 금지: (resetSelectionsUi()로 이미 초기화됨)
    private fun applyAutoSelectResult(clothType: String?, seasons: List<Int>, color: String?, tagCodes: List<Int>) {
        Log.d(TAG, "autoSelect result: clothType=$clothType seasons=$seasons color=$color tagCodes=$tagCodes")

        // 태그/계절: 결과가 비어도 그대로 반영(= 비움 유지)
        if (TagOptions.isReady()) {
            TagOptions.setSelectedTag(tagsSection, tagCodes)
        } else {
            pendingTagCodes = tagCodes
        }
        SeasonOptions.setSelectedSeason(seasonSection, seasons)

        // 옷 종류/색상: 결과가 null이면 선택 없음 유지(이미 resetSelectionsUi로 비어있음)
        clothType?.let { ClothTypeOptions.setClothTypeByEnglish(clothTypeSection, it) }
        color?.let { colorAdapter.setSelectedColor(it) }
    }

    // -----------------------------
    // [중복 금지] 매핑 함수들
    // -----------------------------

    private fun labelsToTokenSet(labels: List<ImageLabel>): Set<String> = labels
        .flatMap { it.text.lowercase().split(Regex("[^a-z0-9]+")) }
        .filter { it.isNotBlank() }
        .toSet()

    private fun mapLabelsToClothType(labels: List<ImageLabel>): String? {
        val score = mutableMapOf<String, Float>().apply {
            clothTypeKeywords.keys.forEach { put(it, 0f) }
        }

        labels.forEach { label ->
            val t = label.text.lowercase()
            val c = label.confidence
            clothTypeKeywords.forEach { (type, keys) ->
                if (keys.any { key -> t.contains(key) }) {
                    score[type] = (score[type] ?: 0f) + c
                }
            }
        }

        val best = score.maxByOrNull { it.value } ?: return null
        return if (best.value >= 0.6f) best.key else null
    }

    private fun mapLabelsToSeasons(labels: List<ImageLabel>): List<Int> {
        val tokens = labelsToTokenSet(labels)
        val seasons = LinkedHashSet<Int>()

        val winterKeys = seasonKeywords["WINTER"].orEmpty()
        val summerKeys = seasonKeywords["SUMMER"].orEmpty()

        val hasWinter = winterKeys.any { tokens.contains(it) }
        val hasSummer = summerKeys.any { tokens.contains(it) }

        if (hasWinter) seasons.add(SeasonOptions.toCode("WINTER") ?: 4)
        if (hasSummer) seasons.add(SeasonOptions.toCode("SUMMER") ?: 2)

        if (seasons.isEmpty()) {
            seasons.add(SeasonOptions.toCode("SPRING") ?: 1)
            seasons.add(SeasonOptions.toCode("FALL") ?: 3)
        } else {
            if (seasons.contains(SeasonOptions.toCode("WINTER") ?: 4)) {
                seasons.add(SeasonOptions.toCode("FALL") ?: 3)
            }
        }

        return seasons.toList()
    }

    private fun mapLabelsToTags(labels: List<ImageLabel>, items: List<OptionItem>): List<Int> {
        if (items.isEmpty()) return emptyList()

        val tokens = labelsToTokenSet(labels)
        val scores = mutableMapOf<Int, Float>() // tagCode -> weighted score

        items.forEach { item ->
            val keys = tagKeywordMap[item.codeKorean.trim()] ?: return@forEach

            labels.forEach { label ->
                val text = label.text.lowercase()
                val confidence = label.confidence

                if (keys.any { key -> text.contains(key) }) {
                    scores[item.code] = (scores[item.code] ?: 0f) + confidence
                }
            }
        }

        val MIN_SCORE = 0.6f

        val filtered = scores
            .filterValues { it >= MIN_SCORE }
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        return filtered
    }

    private fun fallbackTagByLabels(labels: List<ImageLabel>): Int? {
        val t = labels.joinToString(" ") { it.text.lowercase() }

        fun findCode(korean: String): Int? = TagOptions.items.firstOrNull { it.codeKorean == korean }?.code

        if (listOf("sportswear", "sport", "athletic", "fitness", "gym", "running", "training").any { t.contains(it) }) {
            return findCode("운동") ?: findCode("일상")
        }

        if (listOf("suit", "formal", "business", "officewear", "blazer").any { t.contains(it) }) {
            return findCode("출근") ?: findCode("격식있는") ?: findCode("일상")
        }

        return findCode("일상")
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
            val dist =
                (avgR - cr) * (avgR - cr) +
                    (avgG - cg) * (avgG - cg) +
                    (avgB - cb) * (avgB - cb)
            if (dist < bestDist) {
                bestDist = dist
                best = c.codeEnglish
            }
        }
        return best
    }
}
