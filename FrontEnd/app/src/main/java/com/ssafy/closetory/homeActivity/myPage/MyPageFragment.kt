package com.ssafy.closetory.homeActivity.mypage

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.toColorInt
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.authActivity.AuthActivity
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentMyPageBinding
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.dto.StatisticsResponse
import com.ssafy.closetory.homeActivity.mypage.signout.SignoutViewModel
import com.ssafy.closetory.util.ColorOptions
import com.ssafy.closetory.util.auth.AuthManager
import java.lang.reflect.Field
import kotlinx.coroutines.launch

private const val TAG = "MyPageFragment_싸피"

// ✅ (기존) 태그 파이차트용 팔레트 유지
private val TAG_PIE_COLORS = listOf(
    "#0A0F18".toColorInt(), // main black
    "#2B2F36".toColorInt(), // dark gray
    "#4B515C".toColorInt(), // mid gray
    "#8A93A3".toColorInt(), // light gray
    "#D7DCE5".toColorInt(), // very light gray
    "#F1F3F6".toColorInt() // etc (almost white)
)

class MyPageFragment :
    BaseFragment<FragmentMyPageBinding>(
        FragmentMyPageBinding::bind,
        R.layout.fragment_my_page
    ) {

    private lateinit var top3Adapter: Top3ClothesAdapter
    private lateinit var recentCodyAdapter: RecentCodyAdapter
    private val myPageViewModel: MyPageViewModel by viewModels()
    private val signoutViewModel: SignoutViewModel by viewModels()

    private var passwordDialog: AlertDialog? = null
    private var userId = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID) ?: -1
        if (userId == -1) {
            showToast("유저 정보가 없습니다. 다시 로그인 해주세요.")
            moveToLogin()
            return
        }

        // ✅ Top3(코디 히스토리) RecyclerView 초기화/관찰 추가
        setupRecyclerView()
        observeTop3Clothes()

        // 기존 통계 호출 유지 + Top3 호출 추가
        myPageViewModel.getTagsStatistics(userId)
        myPageViewModel.getColorsStatistics(userId)
        myPageViewModel.getTop3Clothes(userId)
        myPageViewModel.getRecentCody() // 최근 코디 조회

        observeUserProfile()
        loadUserProfile()

        binding.btnLogout.setOnClickListener { showLogoutDialog() }
        binding.btnSignout.setOnClickListener { showSignoutDialog() }
        binding.tvEditProfile.setOnClickListener { showPasswordCheckDialog() }

        binding.btnCodyRepository.setOnClickListener {
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.navigation_my_page) {
                navController.navigate(R.id.action_navigation_my_page_to_codyRepositoryFragment)
            }
        }

        observeLogout()
        observeMessage()
        collectSignout()
        observePasswordCheck()
    }

    /* -------------------- ✅ Top3 (코디 히스토리) -------------------- */

    private fun setupRecyclerView() {
        top3Adapter = Top3ClothesAdapter()
        binding.rvTop3Clothes.apply {
            adapter = top3Adapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // 최근 코디
        recentCodyAdapter = RecentCodyAdapter()
        binding.rvRecentCody.apply {
            adapter = recentCodyAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun observeTop3Clothes() {
        myPageViewModel.top3Clothes.observe(viewLifecycleOwner) { clothesList ->
            if (clothesList.isNullOrEmpty()) {
                binding.rvTop3Clothes.visibility = View.GONE
                binding.tvEmptyTop3.visibility = View.VISIBLE
            } else {
                binding.rvTop3Clothes.visibility = View.VISIBLE
                binding.tvEmptyTop3.visibility = View.GONE
                top3Adapter.submitList(clothesList)
            }
        }
    }

    /* -------------------- 프로필 -------------------- */

    private fun bindUserProfile(user: EditProfileInfoResponse) {
        binding.tvNickname.text = user.nickname ?: "닉네임"
        binding.tvHeight.text = "${user.height ?: 0} cm"
        binding.tvWeight.text = "${user.weight ?: 0} kg"

        bindProfileImage(user.profilePhotoUrl)
        bindBodyImage(user.bodyPhotoUrl)
    }

    private fun bindProfileImage(url: String?) {
        if (url.isNullOrBlank()) {
            binding.ivProfile.setImageResource(R.drawable.ic_profile_default)
            return
        }

        com.bumptech.glide.Glide.with(this)
            .load(url)
            .placeholder(R.drawable.ic_profile_default)
            .error(R.drawable.ic_profile_default)
            .into(binding.ivProfile)
    }

    private fun bindBodyImage(url: String?) {
        if (url.isNullOrBlank()) {
            binding.ivBodyPhoto.setImageResource(R.drawable.ic_body_default)
            return
        }

        com.bumptech.glide.Glide.with(this)
            .load(url)
            .placeholder(R.drawable.ic_body_default)
            .error(R.drawable.ic_body_default)
            .into(binding.ivBodyPhoto)
    }

    private fun loadUserProfile() {
        myPageViewModel.loadUserProfile(userId)
    }

    private fun observeUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            myPageViewModel.userProfile.collect { user ->
                bindUserProfile(user)
            }
        }
    }

    /* -------------------- 로그아웃 -------------------- */

    private fun showLogoutDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("로그아웃")
            .setMessage("정말 로그아웃 하시겠습니까?")
            .setPositiveButton("확인") { _, _ -> requestLogout() }
            .setNegativeButton("취소", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(requireContext().getColor(R.color.main_color))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(requireContext().getColor(R.color.gray_500))
    }

    private fun requestLogout() {
        myPageViewModel.logout()
    }

    private fun observeLogout() {
        viewLifecycleOwner.lifecycleScope.launch {
            myPageViewModel.logoutSuccess.collect { success ->
                if (success) {
                    val authManager = AuthManager(requireContext())
                    authManager.clearToken()
                    ApplicationClass.sharedPreferences.clearUserId(ApplicationClass.USERID)
                    ApplicationClass.sharedPreferences.clearUserNickName()

                    Log.d(
                        TAG,
                        "로그아웃 이후 값 확인 : userNickName=${ApplicationClass.sharedPreferences.getUserNickName()}, userId=${ApplicationClass.sharedPreferences.getUserId(
                            ApplicationClass.USERID
                        )}"
                    )

                    val intent = Intent(requireContext(), AuthActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }
    }

    private fun observeMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                myPageViewModel.message.collect { showToast(it) }
            }
        }
    }

    /* -------------------- 비밀번호 확인 -------------------- */

    private fun showPasswordCheckDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_password_check, null)

        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val btnToggle = dialogView.findViewById<ImageButton>(R.id.btnTogglePassword)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        var visible = false

        btnToggle.setOnClickListener {
            visible = togglePasswordVisibility(etPassword, visible)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        passwordDialog = dialog

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val password = etPassword.text.toString()
            if (password.isBlank()) {
                showToast("비밀번호를 입력해주세요.")
                return@setOnClickListener
            }
            myPageViewModel.checkPassword(password)
        }

        dialog.show()
    }

    private fun observePasswordCheck() {
        viewLifecycleOwner.lifecycleScope.launch {
            myPageViewModel.passwordVerified.collect { success ->
                if (success) {
                    passwordDialog?.dismiss()
                    passwordDialog = null
                    findNavController().navigate(R.id.action_navigation_my_page_to_editProfileFragment)
                }
            }
        }

        myPageViewModel.tagsStatistics.observe(viewLifecycleOwner) { list ->
            updatePieTag(list)
        }

        myPageViewModel.colorStatistics.observe(viewLifecycleOwner) { list ->
            updatePieColor(list)
        }

        // 최근 코디 수신
        myPageViewModel.recentCody.observe(viewLifecycleOwner) { codyList ->
            if (codyList.isEmpty()) {
                binding.rvRecentCody.visibility = View.GONE
                binding.tvEmptyRecentCody.visibility = View.VISIBLE
            } else {
                binding.rvRecentCody.visibility = View.VISIBLE
                binding.tvEmptyRecentCody.visibility = View.GONE
                recentCodyAdapter.submitList(codyList)
            }
        }
    }

    private fun togglePasswordVisibility(editText: EditText, isVisible: Boolean): Boolean {
        editText.inputType =
            if (isVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
        editText.setSelection(editText.text.length)
        return !isVisible
    }

    /* -------------------- 회원 탈퇴 -------------------- */

    private fun collectSignout() {
        viewLifecycleOwner.lifecycleScope.launch {
            signoutViewModel.signoutSuccess.collect {
                Toast.makeText(requireContext(), "회원 탈퇴에 성공했습니다.", Toast.LENGTH_SHORT).show()

                ApplicationClass.authManager.clearToken()
                ApplicationClass.sharedPreferences.clearUserNickName()
                ApplicationClass.sharedPreferences.clearUserId(ApplicationClass.USERID)

                Log.d(
                    TAG,
                    "회원 탈퇴 이후 값 확인 : userNickName=${ApplicationClass.sharedPreferences.getUserNickName()}, userId=${ApplicationClass.sharedPreferences.getUserId(
                        ApplicationClass.USERID
                    )}"
                )

                moveToLogin()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            signoutViewModel.message.collect { mes -> showToast(mes) }
        }
    }

    private fun showSignoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_signout, null)

        val etPassword = dialogView.findViewById<EditText>(R.id.etSignoutPassword)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnSignoutConfirm)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnSignoutCancel)
        val btnToggle = dialogView.findViewById<ImageButton>(R.id.btnToggleSignoutPassword)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        var isPasswordVisible = false

        btnToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            etPassword.inputType =
                if (isPasswordVisible) {
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                } else {
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
            etPassword.setSelection(etPassword.text.length)
        }

        btnConfirm.setOnClickListener {
            val password = etPassword.text.toString()
            if (password.isBlank()) {
                Toast.makeText(requireContext(), "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID) ?: -1
            if (uid == -1) {
                Toast.makeText(requireContext(), "유저 정보가 없습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signoutViewModel.signout(uid, password)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun moveToLogin() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    /* -------------------- PieChart (공통) : ✅ 기존 코드 유지 -------------------- */

    private fun applyPieCommon(pieChart: PieChart) {
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setUsePercentValues(true)

        pieChart.setDrawEntryLabels(false)
        pieChart.setDrawCenterText(false)

        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 40f
        pieChart.transparentCircleRadius = 0f

        pieChart.setExtraOffsets(2f, 2f, 2f, 2f)
        pieChart.setMinOffset(0f)

        pieChart.isRotationEnabled = false
        pieChart.isHighlightPerTapEnabled = true
        pieChart.highlightValues(null)
    }

    private fun applyPieDatasetCommon(dataSet: PieDataSet) {
        dataSet.setDrawValues(true)
        dataSet.yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
        dataSet.xValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
        dataSet.sliceSpace = 1f
        dataSet.selectionShift = 6f
    }

    private fun buildPieData(pieChart: PieChart, dataSet: PieDataSet): PieData = PieData(dataSet).apply {
        setValueTextSize(12f)

        // ✅ 기존: 7% 이하는 숨김
        setValueFormatter(object : ValueFormatter() {
            override fun getPieLabel(value: Float, pieEntry: PieEntry?): String =
                if (value <= 7f) "" else String.format("%.1f%%", value)
        })
    }

    /* -------------------- PieChart (태그) -------------------- */

    private fun buildTop5WithEtcEntries(tagCounts: List<Pair<String, Float>>): List<PieEntry> {
        val sorted = tagCounts
            .filter { it.second > 0f }
            .sortedByDescending { it.second }

        if (sorted.isEmpty()) return emptyList()

        val top5 = sorted.take(5)
        val etcSum = sorted.drop(5).sumOf { it.second.toDouble() }.toFloat()

        return buildList {
            top5.forEach { (tag, value) ->
                add(PieEntry(value, "").apply { data = tag })
            }
            if (etcSum > 0f) add(PieEntry(etcSum, "").apply { data = "기타" })
        }
    }

    private fun renderPieTag(entries: List<PieEntry>) {
        val pieChart = binding.pieTag
        applyPieCommon(pieChart)

        if (entries.isEmpty()) {
            pieChart.data = null
            pieChart.invalidate()
            return
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = MutableList(entries.size) { idx ->
                if (idx < 5) TAG_PIE_COLORS[idx] else TAG_PIE_COLORS.last()
            }
        }

        // ✅ 기존: 조각별 글자색 자동
        val valueTextColors = dataSet.colors.map { sliceColor -> idealTextColorOn(sliceColor) }
        dataSet.setValueTextColors(valueTextColors)

        applyPieDatasetCommon(dataSet)
        pieChart.data = buildPieData(pieChart, dataSet)

        // ✅ 기존: 애니메이션 유지
        pieChart.animateY(900, Easing.EaseInOutQuad)

        pieChart.marker = PieMarkerView(requireContext(), R.layout.marker_pie)
        pieChart.invalidate()
    }

    private fun updatePieTag(stats: List<StatisticsResponse>) {
        val pairs = stats.mapNotNull { extractTagAndValue(it) }
        renderPieTag(buildTop5WithEtcEntries(pairs))
    }

    /* -------------------- PieChart (색상) -------------------- */

    private fun buildTop5WithEtcColorEntries(
        colorCounts: List<Pair<String, Float>>
    ): Pair<List<PieEntry>, List<String>> {
        val sorted = colorCounts
            .filter { it.second > 0f }
            .sortedByDescending { it.second }

        if (sorted.isEmpty()) return emptyList<PieEntry>() to emptyList()

        val top5 = sorted.take(5)
        val top5Eng = top5.map { it.first }
        val etcSum = sorted.drop(5).sumOf { it.second.toDouble() }.toFloat()

        val entries = mutableListOf<PieEntry>()
        top5.forEach { (colorEng, value) ->
            val labelKo = ColorOptions.englishToKorean(colorEng) ?: colorEng
            entries.add(PieEntry(value, "").apply { data = labelKo })
        }
        if (etcSum > 0f) entries.add(PieEntry(etcSum, "").apply { data = "기타" })

        return entries to top5Eng
    }

    private fun renderPieColor(entries: List<PieEntry>, top5EngInOrder: List<String>) {
        val pieChart = binding.pieColor
        applyPieCommon(pieChart)

        if (entries.isEmpty()) {
            pieChart.data = null
            pieChart.invalidate()
            return
        }

        val dataSet = PieDataSet(entries, "")

        val colors = mutableListOf<Int>()
        for (i in entries.indices) {
            val c = if (i < top5EngInOrder.size) {
                ColorOptions.englishToArgb(top5EngInOrder[i]) ?: 0xFFBDBDBD.toInt()
            } else {
                0xFFFFFFFF.toInt() // 기타 흰색
            }
            colors.add(c)
        }
        dataSet.colors = colors

        val valueTextColors = colors.map { sliceColor -> idealTextColorOn(sliceColor) }
        dataSet.setValueTextColors(valueTextColors)

        applyPieDatasetCommon(dataSet)
        pieChart.data = buildPieData(pieChart, dataSet)

        pieChart.animateY(900, Easing.EaseInOutQuad)
        pieChart.marker = PieMarkerView(requireContext(), R.layout.marker_pie)
        pieChart.invalidate()
    }

    private fun updatePieColor(stats: List<StatisticsResponse>) {
        fun extractColorAndValue(item: Any): Pair<String, Float>? {
            val colorEng =
                readStringField(item, listOf("color", "colorCode", "codeEnglish", "code", "name"))
                    ?: return null
            val value =
                readNumberField(item, listOf("ratio", "percent", "percentage", "value", "count"))
                    ?: return null
            return colorEng to value
        }

        val pairs = stats.mapNotNull { extractColorAndValue(it) }
        val (entries, top5Eng) = buildTop5WithEtcColorEntries(pairs)
        renderPieColor(entries, top5Eng)
    }

    /* -------------------- ✅ 파이차트 텍스트 색 자동 -------------------- */
    private fun idealTextColorOn(bgColor: Int): Int {
        val r = Color.red(bgColor)
        val g = Color.green(bgColor)
        val b = Color.blue(bgColor)
        val yiq = (r * 299 + g * 587 + b * 114) / 1000
        return if (yiq >= 160) Color.BLACK else Color.WHITE
    }

    /* -------------------- DTO 리플렉션 (유지) -------------------- */

    private fun extractTagAndValue(item: Any): Pair<String, Float>? {
        val tag = readStringField(item, listOf("tagName", "tag", "name")) ?: return null
        val value = readNumberField(item, listOf("ratio", "percent", "percentage", "value", "count")) ?: return null
        return tag to value
    }

    private fun readStringField(target: Any, candidates: List<String>): String? {
        for (name in candidates) {
            val v = readField(target, name) ?: continue
            if (v is String && v.isNotBlank()) return v
        }
        return null
    }

    private fun readNumberField(target: Any, candidates: List<String>): Float? {
        for (name in candidates) {
            val v = readField(target, name) ?: continue
            return when (v) {
                is Float -> v
                is Double -> v.toFloat()
                is Int -> v.toFloat()
                is Long -> v.toFloat()
                is Number -> v.toFloat()
                else -> null
            }
        }
        return null
    }

    private fun readField(target: Any, fieldName: String): Any? = try {
        val f: Field = target.javaClass.getDeclaredField(fieldName)
        f.isAccessible = true
        f.get(target)
    } catch (_: Exception) {
        null
    }
}
