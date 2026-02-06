package com.ssafy.closetory.homeActivity.codyRepository

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
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

private const val TAG = "CodyDetailFragment"

class CodyDetailFragment :
    BaseFragment<FragmentCodyDetailBinding>(
        FragmentCodyDetailBinding::bind,
        R.layout.fragment_cody_detail
    ) {

    private val codyViewModel: CodyRepositoryViewModel by viewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()

    private var lookId: Int = -1
    private var photoUrl: String = ""
    private var originalDate: String = ""
    private var aiReason: String? = null
    private var onlyMine: Boolean = false

    private var selectedCalendarDate: String? = null

    private val calendar: Calendar = Calendar.getInstance()
    private val today: Calendar = Calendar.getInstance()
    private lateinit var calBinding: DialogCalendarPickerBinding
    private lateinit var calendarAdapter: HomeCalendarAdapter

    private var dayColorMap: Map<String, Pair<Int?, Int?>> = emptyMap()
    private var registeredDateSet: Set<String> = emptySet()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        receiveArguments()

        setupEmbeddedCalendar(view)
        applyCalendarSectionVisibility()

        setupUI()
        setupListeners()
        observeViewModels()

        // 홈 캘린더 데이터 로드
        homeViewModel.getStylingList(true)

        // 핵심: aiReason이 비어있으면 lookId로 서버 목록에서 다시 채움
        if (aiReason.isNullOrBlank()) {
            Log.w(TAG, "aiReason이 arguments로 안 들어옴 → lookId로 보강 조회 시도: $lookId")
            codyViewModel.getLooks()
        }

        // 코디 상세 페이지에서 캘린더만 패딩 제거하기
        val calRoot = view.findViewById<View>(R.id.cody_detail_calendar)
        calRoot.setPadding(0, 0, 0, 0)
    }

    private fun receiveArguments() {
        arguments?.let { bundle ->
            lookId = bundle.getInt("lookId", -1)
            photoUrl = bundle.getString("photoUrl", "")
            originalDate = bundle.getString("date", "") ?: ""

            // ✅ defaultValue로 안전하게 받고, blank면 null 처리
            val reasonArg = bundle.getString("aiReason", "") ?: ""
            aiReason = reasonArg.takeIf { it.isNotBlank() }

            onlyMine = bundle.getBoolean("onlyMine", false)

            Log.d(
                TAG,
                "받은 데이터 - lookId=$lookId, date='$originalDate', onlyMine=$onlyMine, aiReasonLen=${aiReason?.length ?: 0}"
            )
        }
    }

    private fun applyCalendarSectionVisibility() {
        val showCalendarSection = onlyMine

        binding.tvCalendarLabel.visibility = if (showCalendarSection) View.VISIBLE else View.GONE
        binding.layoutSelectedDate.visibility = if (showCalendarSection) View.VISIBLE else View.GONE
        binding.cardRegister.visibility = if (showCalendarSection) View.VISIBLE else View.GONE

        binding.root.findViewById<View>(R.id.cody_detail_calendar)?.visibility =
            if (showCalendarSection) View.VISIBLE else View.GONE

        if (!showCalendarSection) selectedCalendarDate = null
    }

    private fun setupEmbeddedCalendar(root: View) {
        bindCalendarInclude(root)
        setupWeekHeader()
        setupCalendarGrid()

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        renderMonth()

        calBinding.btnPrev.setOnClickListener { moveMonthAndRender(-1) }
        calBinding.btnNext.setOnClickListener { moveMonthAndRender(1) }

        calBinding.btnConfirm.visibility = View.GONE
        calBinding.btnCancel.visibility = View.GONE
        calBinding.tvTitle.visibility = View.GONE
    }

    private fun bindCalendarInclude(root: View) {
        val includeRoot = root.findViewById<View>(R.id.cody_detail_calendar)
        requireNotNull(includeRoot) { "cody_detail_calendar(include) not found in fragment_cody_detail.xml" }
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
                if (!onlyMine) return@HomeCalendarAdapter
                onCalendarDateSelected(day)
            },
            colorProvider = { day ->
                dayColorMap[keyOf(day)] ?: (null to null)
            },
            isBlocked = { false }
        )

        calBinding.rvCalendar.adapter = calendarAdapter
    }

    private fun setupUI() {
        val fullphotoUrl = if (photoUrl.startsWith("http")) {
            photoUrl
        } else {
            val clean = photoUrl.removePrefix("/")
            "${ApplicationClass.API_BASE_URL}$clean"
        }

        Glide.with(this)
            .load(fullphotoUrl)
            .centerCrop()
            .placeholder(R.drawable.bg_slot_empty)
            .error(R.drawable.error)
            .into(binding.ivCodyImage)

        if (onlyMine) {
            if (originalDate.isNotBlank()) {
                binding.tvSelectedDate.text = formatDateDisplay(originalDate)
                binding.ivCalendarIcon.visibility = View.GONE
                selectedCalendarDate = originalDate
                updateRegisterButton(true)
            } else {
                binding.tvSelectedDate.text = "날짜를 선택해 주세요."
                binding.ivCalendarIcon.visibility = View.VISIBLE
                updateRegisterButton(false)
            }
        }

        applyAiReasonToUI()
    }

    private fun applyAiReasonToUI() {
        if (!aiReason.isNullOrBlank()) {
            binding.tvReasonLabel.visibility = View.VISIBLE
            binding.tvReason.visibility = View.VISIBLE
            binding.tvReason.text = aiReason
        } else {
            binding.tvReasonLabel.visibility = View.GONE
            binding.tvReason.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnRegister.setOnClickListener {
            if (!onlyMine) return@setOnClickListener
            if (selectedCalendarDate != null) registerToCalendar()
        }

        binding.btnDelete.setOnClickListener { showDeleteConfirmDialog() }
    }

    private fun observeViewModels() {
        // ✅ 보강 데이터: 목록에서 lookId 매칭해 aiReason 채움
        codyViewModel.looks.observe(viewLifecycleOwner) { list ->
            if (!aiReason.isNullOrBlank()) return@observe
            val found = list.firstOrNull { it.lookId == lookId } ?: return@observe

            Log.d(TAG, "lookId=$lookId 보강 성공 - aiReasonLen=${found.aiReason?.length ?: 0}")

            // 필요한 값들 보강(안전)
            aiReason = found.aiReason?.takeIf { it.isNotBlank() }
            // (선택) 서버 값이 더 정확하면 여기서 업데이트 가능
            // photoUrl = found.photoUrl
            // originalDate = found.date ?: originalDate
            // onlyMine = found.onlyMine

            applyAiReasonToUI()
        }

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

        codyViewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                codyViewModel.clearSuccessMessage()

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
            if (onlyMine) {
                binding.btnRegister.isEnabled = !isLoading && selectedCalendarDate != null
            }
            binding.btnDelete.isEnabled = !isLoading
        }
    }

    private fun onCalendarDateSelected(day: Day) {
        val dateKey = keyOf(day)
        Log.d(TAG, "날짜 선택: $dateKey")

        if (registeredDateSet.contains(dateKey)) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("날짜 변경 확인")
                .setMessage("$dateKey 에 이미 다른 룩이 등록되어 있습니다.\n변경하시겠습니까?")
                .setPositiveButton("변경") { _, _ -> applySelectedDate(day, dateKey) }
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

    private fun registerToCalendar() {
        val date = selectedCalendarDate ?: return
        Log.d(TAG, "캘린더 등록 - lookId: $lookId, date: $date")
        codyViewModel.registerToCalendar(lookId, date)
    }

    private fun showDeleteConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("룩 삭제")
            .setMessage("이 룩을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ -> codyViewModel.deleteLook(lookId) }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateRegisterButton(isEnabled: Boolean) {
        if (!onlyMine) return

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

    private fun moveMonthAndRender(deltaMonth: Int) {
        calendar.add(Calendar.MONTH, deltaMonth)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        renderMonth()
    }

    private fun renderMonth() {
        val year = calendar.get(Calendar.YEAR)
        val month0 = calendar.get(Calendar.MONTH)
        calBinding.tvMonth.text = "%04d년 %02d월".format(year, month0 + 1)

        val days = build42Days(year, month0)
        calendarAdapter.submitList(days)

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

        for (d in 1..thisLast) {
            val c = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month0)
                set(Calendar.DAY_OF_MONTH, d)
            }
            list.add(makeDay(c, inMonth = true))
        }

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
