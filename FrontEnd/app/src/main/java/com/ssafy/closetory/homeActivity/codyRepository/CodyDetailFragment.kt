// package com.ssafy.closetory.homeActivity.codyDetail
//
// import android.os.Bundle
// import android.util.Log
// import android.view.View
// import android.widget.Toast
// import androidx.fragment.app.setFragmentResultListener
// import androidx.navigation.fragment.findNavController
// import com.bumptech.glide.Glide
// import com.ssafy.closetory.ApplicationClass
// import com.ssafy.closetory.R
// import com.ssafy.closetory.baseCode.base.BaseFragment
// import com.ssafy.closetory.databinding.FragmentCodyDetailBinding
// import com.ssafy.closetory.homeActivity.aiStyling.CalendarPickerDialogFragment
//
// private const val TAG = "CodyDetailFragment"
//
// class CodyDetailFragment :
//    BaseFragment<FragmentCodyDetailBinding>(
//        FragmentCodyDetailBinding::bind,
//        R.layout.fragment_cody_detail
//    ) {
//
//    private var lookId: Int = -1
//    private var photoUrl: String = ""
//    private var date: String = ""
//    private var aiReason: String? = null
//    private var onlyMine: Boolean = false
//
//    private var selectedCalendarDate: String? = null
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        Log.d(TAG, "CodyDetailFragment onViewCreated")
//
//        // Bundle에서 데이터 받기
//        receiveArguments()
//
//        // 캘린더 결과 리스너 설정
//        setupCalendarResultListener()
//
//        setupUI()
//        setupListeners()
//    }
//
//    private fun receiveArguments() {
//        arguments?.let { bundle ->
//            lookId = bundle.getInt("lookId", -1)
//            photoUrl = bundle.getString("photoUrl", "")
//            date = bundle.getString("date", "")
//            aiReason = bundle.getString("aiReason")
//            onlyMine = bundle.getBoolean("onlyMine", false)
//
//            Log.d(TAG, "받은 데이터 - lookId: $lookId, date: $date, aiReason: $aiReason")
//        }
//    }
//
//    //    private fun setupCalendarResultListener() {
// //        // CalendarPickerDialogFragment에서 날짜 선택 결과 받기
// //        setFragmentResultListener(CalendarPickerDialogFragment.REQ_KEY) { _, bundle ->
// //            val pickedDate = bundle.getString(CalendarPickerDialogFragment.BUNDLE_KEY_DATE)
// //            if (pickedDate != null) {
// //                onCalendarDateSelected(pickedDate)
// //            }
// //        }
// //    }
//    private fun setupCalendarResultListener() {
//        parentFragmentManager.setFragmentResultListener(
//            CalendarPickerDialogFragment.REQ_KEY,
//            viewLifecycleOwner
//        ) { _, bundle ->
//            val pickedDate = bundle.getString(CalendarPickerDialogFragment.BUNDLE_KEY_DATE)
//            if (!pickedDate.isNullOrBlank()) {
//                onCalendarDateSelected(pickedDate)
//            }
//        }
//    }
//
//    private fun setupUI() {
//        // 이미지 로딩
//        val fullImageUrl = if (photoUrl.startsWith("http")) {
//            photoUrl
//        } else {
//            "${ApplicationClass.API_BASE_URL}$photoUrl"
//        }
//
//        Glide.with(this)
//            .load(fullImageUrl)
//            .centerCrop()
//            .placeholder(R.drawable.bg_slot_empty)
//            .error(R.drawable.error)
//            .into(binding.ivCodyImage)
//
//        // 날짜 표시
//        binding.tvDate.text = date
//
//        // AI 추천 이유 표시 (있을 경우에만)
//        if (!aiReason.isNullOrBlank()) {
//            binding.tvReasonLabel.visibility = View.VISIBLE
//            binding.tvReason.visibility = View.VISIBLE
//            binding.tvReason.text = aiReason
//        } else {
//            binding.tvReasonLabel.visibility = View.GONE
//            binding.tvReason.visibility = View.GONE
//        }
//
//        // 초기 등록 버튼 상태 (비활성화)
//        updateRegisterButton(false)
//    }
//
//    private fun setupListeners() {
//        // 뒤로가기 버튼
//        binding.btnBack.setOnClickListener {
//            findNavController().popBackStack()
//        }
//
//        // 캘린더 버튼 클릭 - 캘린더 다이얼로그 표시
//        binding.btnCalendar.setOnClickListener {
//            showCalendarDialog()
//        }
//
//        // 등록 버튼 클릭
//        binding.btnRegister.setOnClickListener {
//            if (!onlyMine) {
//                Toast.makeText(requireContext(), "내 옷만 포함된 코디만 등록할 수 있어요.", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (selectedCalendarDate.isNullOrBlank()) {
//                Toast.makeText(requireContext(), "날짜를 먼저 선택해 주세요.", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            registerToCalendar()
//        }
//    }
//
//    private fun showCalendarDialog() {
//        Log.d(TAG, "캘린더 다이얼로그 표시")
//        val dialog = CalendarPickerDialogFragment()
//        dialog.show(parentFragmentManager, "CalendarPicker")
//    }
//
// //    private fun onCalendarDateSelected(dateStr: String) {
// //        // dateStr 형식: "yyyy\nMM-dd" 또는 "yyyy-MM-dd"
// //        Log.d(TAG, "캘린더에서 선택한 날짜: $dateStr")
// //
// //        // 줄바꿈 제거하고 형식 통일
// //        selectedCalendarDate = dateStr.replace("\n", "-")
// //
// //        // 캘린더 아이콘 숨기고 날짜 표시
// //        binding.ivCalendarIcon.visibility = View.GONE
// //        binding.tvSelectedDate.text = selectedCalendarDate
// //
// //        // 등록 버튼 활성화
// //        updateRegisterButton(true)
// //
// //        Toast.makeText(requireContext(), "날짜가 선택되었습니다", Toast.LENGTH_SHORT).show()
// //    }
//    private fun onCalendarDateSelected(dateStr: String) {
//        // 이제 dateStr은 "yyyy-MM-dd" 로 온다고 가정
//        Log.d(TAG, "캘린더에서 선택한 날짜: $dateStr")
//        selectedCalendarDate = dateStr
//
//        // 캘린더 아이콘 숨기고 날짜 표시
//        binding.ivCalendarIcon.visibility = View.GONE
//        binding.tvSelectedDate.text = dateStr
//
//        // 등록날짜(tvDate)에도 반영
//        binding.tvDate.text = dateStr
//
//        // onlyMine=true일 때만 등록 버튼 활성화
//        updateRegisterButton(isEnabled = (onlyMine && selectedCalendarDate != null))
//
//        if (!onlyMine) {
//            Toast.makeText(requireContext(), "내 옷만 포함된 코디만 등록할 수 있어요.", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun updateRegisterButton(isEnabled: Boolean) {
//        binding.btnRegister.isEnabled = isEnabled
//
//        if (isEnabled) {
//            // 활성화 상태 - 메인 컬러
//            binding.cardRegister.setCardBackgroundColor(
//                resources.getColor(R.color.main_color, null)
//            )
//            binding.btnRegister.alpha = 1.0f
//        } else {
//            // 비활성화 상태 - 회색
//            binding.cardRegister.setCardBackgroundColor(
//                resources.getColor(android.R.color.darker_gray, null)
//            )
//            binding.btnRegister.alpha = 0.5f
//        }
//    }
//
//    private fun registerToCalendar() {
//        Log.d(TAG, "캘린더에 등록 - lookId: $lookId, date: $selectedCalendarDate")
//
//        // 예: viewModel.registerLookToCalendar(lookId, selectedCalendarDate)
//
//        Toast.makeText(
//            requireContext(),
//            "$selectedCalendarDate 에 코디가 등록되었습니다!",
//            Toast.LENGTH_LONG
//        ).show()
//
//        // 등록 후 뒤로가기
//        findNavController().popBackStack()
//    }
// }

package com.ssafy.closetory.homeActivity.codyRepository

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.DialogCalendarPickerBinding
import com.ssafy.closetory.databinding.FragmentCodyDetailBinding
import com.ssafy.closetory.homeActivity.adapter.HomeCalendarAdapter
import com.ssafy.closetory.homeActivity.aiStyling.Day
import com.ssafy.closetory.homeActivity.aiStyling.WeekAdapter
import com.ssafy.closetory.homeActivity.home.HomeViewModel
import com.ssafy.closetory.util.ColorOptions
import java.util.Calendar
import kotlinx.coroutines.launch

private const val TAG = "CodyDetailFragment"

class CodyDetailFragment :
    BaseFragment<FragmentCodyDetailBinding>(
        FragmentCodyDetailBinding::bind,
        R.layout.fragment_cody_detail
    ) {

    // 코디 저장소 ViewModel
    private val codyViewModel: CodyRepositoryViewModel by viewModels()

    // 홈 ViewModel (캘린더 데이터 공유)
    private val homeViewModel: HomeViewModel by activityViewModels()

    private var lookId: Int = -1
    private var photoUrl: String = ""
    private var originalDate: String = "" // 기존 등록 날짜
    private var aiReason: String? = null
    private var onlyMine: Boolean = false

    private var selectedCalendarDate: String? = null

    // 캘린더 관련
    private val calendar: Calendar = Calendar.getInstance()
    private val today: Calendar = Calendar.getInstance()
    private lateinit var calBinding: DialogCalendarPickerBinding
    private lateinit var calendarAdapter: HomeCalendarAdapter

    // 서버 데이터
    private var dayColorMap: Map<String, Pair<Int?, Int?>> = emptyMap()
    private var registeredDateSet: Set<String> = emptySet()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "CodyDetailFragment onViewCreated")

        receiveArguments()
        setupEmbeddedCalendar(view)
        setupUI()
        setupListeners()
        observeViewModels()

        // 홈 캘린더 데이터 로드
        homeViewModel.getStylingList(true)
    }

    /**
     * Bundle 데이터 받기
     */
    private fun receiveArguments() {
        arguments?.let { bundle ->
            lookId = bundle.getInt("lookId", -1)
            photoUrl = bundle.getString("photoUrl", "")
            originalDate = bundle.getString("date", "")
            aiReason = bundle.getString("aiReason")
            onlyMine = bundle.getBoolean("onlyMine", false)

            Log.d(TAG, "받은 데이터 - lookId: $lookId, originalDate: $originalDate")
        }
    }

    /**
     * 임베디드 캘린더 설정 (HomeFragment 로직 그대로 사용)
     */
    private fun setupEmbeddedCalendar(root: View) {
        bindCalendarInclude(root)
        setupWeekHeader()
        setupCalendarGrid()

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        renderMonth()

        calBinding.btnPrev.setOnClickListener { moveMonthAndRender(-1) }
        calBinding.btnNext.setOnClickListener { moveMonthAndRender(1) }

        // 다이얼로그용 버튼은 숨김
        calBinding.btnConfirm.visibility = View.GONE
        calBinding.btnCancel.visibility = View.GONE
        calBinding.tvTitle.visibility = View.GONE
    }

    private fun bindCalendarInclude(root: View) {
        val includeRoot = root.findViewById<View>(R.id.cody_detail_calendar)
        calBinding = DialogCalendarPickerBinding.bind(includeRoot)
    }

    private fun setupWeekHeader() {
        calBinding.rvWeeklist.layoutManager = GridLayoutManager(requireContext(), 7)
        calBinding.rvWeeklist.adapter = WeekAdapter(listOf("일", "월", "화", "수", "목", "금", "토"))
    }

    private fun setupCalendarGrid() {
        calBinding.rvCalendar.layoutManager = GridLayoutManager(requireContext(), 7)

        calendarAdapter = HomeCalendarAdapter(
            items = emptyList(),
            onClick = { day ->
                if (!day.inMonth) return@HomeCalendarAdapter
                onCalendarDateSelected(day)
            },
            colorProvider = { day ->
                dayColorMap[keyOf(day)] ?: (null to null)
            },
            isBlocked = { day ->
                // 이미 등록된 날짜는 차단하지 않음 (수정 가능하게)
                false
            }
        )

        calBinding.rvCalendar.adapter = calendarAdapter
    }

    /**
     * UI 초기 설정
     */
    private fun setupUI() {
        // 이미지 로딩
        val fullImageUrl = if (photoUrl.startsWith("http")) {
            photoUrl
        } else {
            val clean = photoUrl.removePrefix("/")
            "${ApplicationClass.API_BASE_URL}$clean"
        }

        Glide.with(this)
            .load(fullImageUrl)
            .centerCrop()
            .placeholder(R.drawable.bg_slot_empty)
            .error(R.drawable.error)
            .into(binding.ivCodyImage)

        // 기존 등록 날짜 표시
        if (originalDate.isNotBlank()) {
            binding.tvSelectedDate.text = formatDateDisplay(originalDate)
            binding.ivCalendarIcon.visibility = View.GONE
            selectedCalendarDate = originalDate
            updateRegisterButton(true)
        } else {
            binding.tvSelectedDate.text = "날짜를 선택해주세요"
            binding.ivCalendarIcon.visibility = View.VISIBLE
            updateRegisterButton(false)
        }

        // AI 추천 이유 표시
        if (!aiReason.isNullOrBlank()) {
            binding.tvReasonLabel.visibility = View.VISIBLE
            binding.tvReason.visibility = View.VISIBLE
            binding.tvReason.text = aiReason
        } else {
            binding.tvReasonLabel.visibility = View.GONE
            binding.tvReason.visibility = View.GONE
        }
    }

    /**
     * 리스너 설정
     */
    private fun setupListeners() {
        // 뒤로가기
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 등록/수정 버튼
        binding.btnRegister.setOnClickListener {
            if (selectedCalendarDate != null) {
                registerToCalendar()
            }
        }

        // 삭제 버튼
        binding.btnDelete.setOnClickListener {
            showDeleteConfirmDialog()
        }
    }

    /**
     * ViewModel 관찰
     */
    private fun observeViewModels() {
        // 홈 ViewModel - 캘린더 데이터
        homeViewModel.dayColorMap.observe(viewLifecycleOwner) { colorMap ->
            dayColorMap = colorMap.mapValues { (_, colors) ->
                val (topName, bottomName) = colors
                val top = ColorOptions.englishToArgb(topName)
                val bottom = ColorOptions.englishToArgb(bottomName)
                top to bottom
            }
            renderMonth()
        }

        homeViewModel.registeredDateSet.observe(viewLifecycleOwner) { dateSet ->
            registeredDateSet = dateSet
            Log.d(TAG, "등록된 날짜 Set: $registeredDateSet")
        }

        // 코디 ViewModel - 등록/삭제 결과
        codyViewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                codyViewModel.clearSuccessMessage()

                // 성공 후 홈 캘린더 새로고침 & 뒤로가기
                homeViewModel.getStylingList(true)
                findNavController().popBackStack()
            }
        }

        codyViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                codyViewModel.clearErrorMessage()
            }
        }

        codyViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnRegister.isEnabled = !isLoading && selectedCalendarDate != null
            binding.btnDelete.isEnabled = !isLoading
        }
    }

    /**
     * 캘린더 날짜 선택
     */
    private fun onCalendarDateSelected(day: Day) {
        val dateKey = keyOf(day)
        Log.d(TAG, "날짜 선택: $dateKey")

        // 이미 등록된 날짜인지 확인
        if (registeredDateSet.contains(dateKey)) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("날짜 변경 확인")
                .setMessage("$dateKey 에 이미 다른 룩이 등록되어 있습니다.\n변경하시겠습니까?")
                .setPositiveButton("변경") { _, _ ->
                    applySelectedDate(day, dateKey)
                }
                .setNegativeButton("취소", null)
                .show()
        } else {
            applySelectedDate(day, dateKey)
        }

        calendarAdapter.setSelected(day)
    }

    private fun applySelectedDate(day: Day, dateKey: String) {
        selectedCalendarDate = dateKey
        binding.ivCalendarIcon.visibility = View.GONE
        binding.tvSelectedDate.text = formatDateDisplay(dateKey)
        updateRegisterButton(true)

        Log.d(TAG, "날짜 적용: $dateKey")
    }

    /**
     * 캘린더에 등록
     */
    private fun registerToCalendar() {
        val date = selectedCalendarDate ?: return

        Log.d(TAG, "캘린더 등록 - lookId: $lookId, date: $date")

        codyViewModel.registerToCalendar(lookId, date)
    }

    /**
     * 룩 삭제 확인 다이얼로그
     */
    private fun showDeleteConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("룩 삭제")
            .setMessage("이 룩을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                codyViewModel.deleteLook(lookId)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    /**
     * 등록 버튼 상태 업데이트
     */
    private fun updateRegisterButton(isEnabled: Boolean) {
        binding.btnRegister.isEnabled = isEnabled

        if (isEnabled) {
            binding.cardRegister.setCardBackgroundColor(
                resources.getColor(R.color.main_color, null)
            )
            binding.btnRegister.alpha = 1.0f
        } else {
            binding.cardRegister.setCardBackgroundColor(
                resources.getColor(android.R.color.darker_gray, null)
            )
            binding.btnRegister.alpha = 0.5f
        }
    }

    /**
     * 월 이동 및 렌더링
     */
    private fun moveMonthAndRender(deltaMonth: Int) {
        calendar.add(Calendar.MONTH, deltaMonth)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        renderMonth()
    }

    /**
     * 월 렌더링
     */
    private fun renderMonth() {
        val year = calendar.get(Calendar.YEAR)
        val month0 = calendar.get(Calendar.MONTH)
        calBinding.tvMonth.text = "%04d년%02d월".format(year, month0 + 1)

        val days = build42Days(year, month0)
        calendarAdapter.submitList(days)

        // 선택된 날짜가 있으면 유지
        selectedCalendarDate?.let { dateStr ->
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                val y = parts[0].toIntOrNull()
                val m = parts[1].toIntOrNull()?.minus(1)
                val d = parts[2].toIntOrNull()

                if (y == year && m == month0 && d != null) {
                    days.firstOrNull { it.inMonth && it.dayOfMonth == d }?.let { day ->
                        calendarAdapter.setSelected(day)
                    }
                }
            }
        }
    }

    /**
     * 42일 생성 (HomeFragment와 동일)
     */
    private fun build42Days(year: Int, month0: Int): List<Day> {
        val list = mutableListOf<Day>()

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month0)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val firstDow = cal.get(Calendar.DAY_OF_WEEK)
        val thisLast = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val prev = (cal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        val prevLast = prev.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 이전 달
        val prevCount = firstDow - 1
        val startPrevDay = prevLast - prevCount + 1
        for (d in startPrevDay..prevLast) {
            val c = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month0)
                add(Calendar.MONTH, -1)
                set(Calendar.DAY_OF_MONTH, d)
            }
            list.add(makeDay(c, inMonth = false))
        }

        // 이번 달
        for (d in 1..thisLast) {
            val c = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month0)
                set(Calendar.DAY_OF_MONTH, d)
            }
            list.add(makeDay(c, inMonth = true))
        }

        // 다음 달
        var nextDay = 1
        while (list.size < 42) {
            val c = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month0)
                add(Calendar.MONTH, 1)
                set(Calendar.DAY_OF_MONTH, nextDay)
            }
            list.add(makeDay(c, inMonth = false))
            nextDay++
        }

        return list
    }

    private fun makeDay(c: Calendar, inMonth: Boolean): Day {
        val y = c.get(Calendar.YEAR)
        val m0 = c.get(Calendar.MONTH)
        val d = c.get(Calendar.DAY_OF_MONTH)
        val dow = c.get(Calendar.DAY_OF_WEEK)

        val isToday =
            y == today.get(Calendar.YEAR) &&
                m0 == today.get(Calendar.MONTH) &&
                d == today.get(Calendar.DAY_OF_MONTH)

        return Day(
            dayText = d.toString(),
            inMonth = inMonth,
            year = y,
            month0 = m0,
            dayOfMonth = d,
            dayOfWeek = dow,
            isToday = isToday
        )
    }

    private fun keyOf(d: Day): String = "%04d-%02d-%02d".format(d.year, d.month0 + 1, d.dayOfMonth)

    /**
     * 날짜 표시 포맷: "2026-01-15" → "2026년 1월 15일"
     */
    private fun formatDateDisplay(dateStr: String): String = try {
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            "${year}년 ${month}월 ${day}일"
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}
