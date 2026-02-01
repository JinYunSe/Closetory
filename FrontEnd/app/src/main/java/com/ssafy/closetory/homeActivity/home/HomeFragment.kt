package com.ssafy.closetory.homeActivity.home

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.DialogCalendarPickerBinding
import com.ssafy.closetory.databinding.FragmentHomeBinding
import com.ssafy.closetory.dto.StylingResponse
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.homeActivity.adapter.HomeCalendarAdapter
import com.ssafy.closetory.homeActivity.aiStyling.Day
import com.ssafy.closetory.homeActivity.aiStyling.WeekAdapter
import com.ssafy.closetory.util.ColorOptions
import java.util.Calendar
import java.util.Date

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::bind, R.layout.fragment_home) {

    private val calendar: Calendar = Calendar.getInstance()
    private val today: Calendar = Calendar.getInstance()

    private lateinit var calBinding: DialogCalendarPickerBinding
    private lateinit var homeCalendarAdapter: HomeCalendarAdapter
    private var selectedDay: Day? = null

    private lateinit var homeActivity: HomeActivity
    private val homeViewModel: HomeViewModel by viewModels()

    // ✅ 날짜별 (상의색, 하의색) 캐시
    private var dayColorMap: Map<String, Pair<Int?, Int?>> = emptyMap()

    // ✅ 날짜 다이얼로그 중복 방지(선택)
    private var selectedDateDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireContext() as HomeActivity

        setupClickPostBtn()
        setupEmbeddedCalendar(view)

        observeStylingList()
        requestCurrentMonthStyling()
    }

    private fun setupClickPostBtn() {
        binding.btnPosts.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_post_list)
        }
    }

    private fun setupEmbeddedCalendar(root: View) {
        bindCalendarInclude(root)
        setupWeekHeader()
        setupCalendarGrid()

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        renderMonth()

        setupMonthNavButtons()

        // 현재 버튼은 GONE이지만 남겨도 됨
        setupEmbeddedActionButtons()
    }

    private fun bindCalendarInclude(root: View) {
        val includeRoot = root.findViewById<View>(R.id.home_calender)
        calBinding = DialogCalendarPickerBinding.bind(includeRoot)

        calBinding.tvTitle.visibility = View.GONE
        calBinding.btnConfirm.visibility = View.GONE
        calBinding.btnCancel.visibility = View.GONE
    }

    private fun setupWeekHeader() {
        calBinding.rvWeeklist.layoutManager = GridLayoutManager(homeActivity, 7)
        calBinding.rvWeeklist.adapter = WeekAdapter(listOf("일", "월", "화", "수", "목", "금", "토"))
    }

    private fun setupCalendarGrid() {
        calBinding.rvCalendar.layoutManager = GridLayoutManager(homeActivity, 7)

        homeCalendarAdapter = HomeCalendarAdapter(
            items = emptyList(),
            onClick = { day ->
                selectedDay = day
                homeCalendarAdapter.setSelected(day)
                showSelectedDateDialog(day)
            },
            colorProvider = { day ->
                dayColorMap[keyOf(day)] ?: (null to null)
            }
        )

        calBinding.rvCalendar.adapter = homeCalendarAdapter
    }

    private fun setupMonthNavButtons() {
        calBinding.btnPrev.setOnClickListener { moveMonthAndFetch(-1) }
        calBinding.btnNext.setOnClickListener { moveMonthAndFetch(1) }
    }

    private fun moveMonthAndFetch(deltaMonth: Int) {
        calendar.add(Calendar.MONTH, deltaMonth)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        // 월 텍스트/그리드는 즉시 갱신
        renderMonth()

        // 해당 월 데이터 요청
        requestCurrentMonthStyling()
    }

    private fun setupEmbeddedActionButtons() {
        calBinding.btnCancel.setOnClickListener { clearSelectionAndRender() }
        calBinding.btnConfirm.setOnClickListener { handleConfirmSelection() }
    }

    private fun clearSelectionAndRender() {
        selectedDay = null
        renderMonth()
    }

    private fun handleConfirmSelection() {
        val dateStr = selectedDay?.let { formatDay(it) } ?: formatToday()
        // TODO 사용처 연결
        // binding.tvPickedDate.text = dateStr
    }

    private fun formatToday(): String {
        val y = today.get(Calendar.YEAR)
        val m0 = today.get(Calendar.MONTH)
        val d = today.get(Calendar.DAY_OF_MONTH)
        return "%04d-%02d-%02d".format(y, m0 + 1, d)
    }

    private fun formatDay(day: Day): String = "%04d-%02d-%02d".format(day.year, day.month0 + 1, day.dayOfMonth)

    private fun renderMonth() {
        val year = calendar.get(Calendar.YEAR)
        val month0 = calendar.get(Calendar.MONTH)

        calBinding.tvMonth.text = "%04d년 %02d월".format(year, month0 + 1)

        val days = build42Days(year, month0)
        homeCalendarAdapter.submitList(days)

        applyDefaultSelectionIfTodayInThisMonth(year, month0, days)
    }

    private fun applyDefaultSelectionIfTodayInThisMonth(year: Int, month0: Int, days: List<Day>) {
        val isSameMonth = today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month0
        if (!isSameMonth) return

        val todayDay = today.get(Calendar.DAY_OF_MONTH)
        val found = days.firstOrNull { it.inMonth && it.dayOfMonth == todayDay }
        if (found != null) {
            selectedDay = found
            homeCalendarAdapter.setSelected(found)
        }
    }

    private fun build42Days(year: Int, month0: Int): List<Day> {
        val list = mutableListOf<Day>()

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month0)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDow = cal.get(Calendar.DAY_OF_WEEK)
        val thisLast = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val prev = cal.clone() as Calendar
        prev.add(Calendar.MONTH, -1)
        val prevLast = prev.getActualMaximum(Calendar.DAY_OF_MONTH)

        val prevCount = firstDow - 1
        val startPrevDay = prevLast - prevCount + 1
        for (d in startPrevDay..prevLast) {
            val c = Calendar.getInstance()
            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, month0)
            c.add(Calendar.MONTH, -1)
            c.set(Calendar.DAY_OF_MONTH, d)
            list.add(makeDay(c, inMonth = false))
        }

        for (d in 1..thisLast) {
            val c = Calendar.getInstance()
            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, month0)
            c.set(Calendar.DAY_OF_MONTH, d)
            list.add(makeDay(c, inMonth = true))
        }

        var nextDay = 1
        while (list.size < 42) {
            val c = Calendar.getInstance()
            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, month0)
            c.add(Calendar.MONTH, 1)
            c.set(Calendar.DAY_OF_MONTH, nextDay)
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

        val isToday = (
            y == today.get(Calendar.YEAR) &&
                m0 == today.get(Calendar.MONTH) &&
                d == today.get(Calendar.DAY_OF_MONTH)
            )

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

    private fun requestCurrentMonthStyling() {
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH) + 1
        homeViewModel.getStylingList(y, m) // ✅ 너 ViewModel 함수명에 맞추기
    }

    private fun observeStylingList() {
        homeViewModel.stylingList.observe(viewLifecycleOwner) { list ->
            dayColorMap = buildDayColorMap(list)
            renderMonth()
        }
    }

    /**
     * ✅ list -> 날짜별 (상의색, 하의색) 맵
     * 아래 필드명은 너 StylingResponse에 맞게 수정 필요:
     * - date: "yyyy-MM-dd"
     * - topColor: "RED" 같은 영문 코드
     * - bottomColor: "BLUE" 같은 영문 코드
     */
    private fun buildDayColorMap(list: List<StylingResponse>): Map<Date, Pair<Int?, Int?>> {
        val byDate = list.groupBy { it.date } // TODO 필드명 맞추기

        return byDate.mapValues { (_, items) ->
            val item = items.last() // TODO 최신 기준 필요하면 정렬로 변경

            val topArgb = ColorOptions.englishToArgb(item.topColor) // TODO 필드명 맞추기
            val bottomArgb = ColorOptions.englishToArgb(item.bottomColor) // TODO 필드명 맞추기

            topArgb to bottomArgb
        }
    }

    private fun keyOf(d: Day): String {
        val m = d.month0 + 1
        return "%04d-%02d-%02d".format(d.year, m, d.dayOfMonth)
    }

    private fun showSelectedDateDialog(day: Day) {
        selectedDateDialog?.dismiss()
        selectedDateDialog = null

        val v = layoutInflater.inflate(R.layout.dialog_main_calendar_selected_date, null)
        val ivClose = v.findViewById<ImageView>(R.id.iv_home_fragment_close)

        val dialog = AlertDialog.Builder(homeActivity)
            .setView(v)
            .create()

        ivClose.setOnClickListener { dialog.dismiss() }

        dialog.setOnDismissListener {
            if (selectedDateDialog === dialog) selectedDateDialog = null
        }

        selectedDateDialog = dialog
        dialog.show()
    }

    override fun onDestroyView() {
        selectedDateDialog?.dismiss()
        selectedDateDialog = null
        super.onDestroyView()
    }
}
