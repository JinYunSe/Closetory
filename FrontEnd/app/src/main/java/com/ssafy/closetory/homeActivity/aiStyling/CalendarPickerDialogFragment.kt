package com.ssafy.closetory.homeActivity.aiStyling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.GridLayoutManager
import com.ssafy.closetory.R
import com.ssafy.closetory.databinding.DialogCalendarPickerBinding
import java.util.Calendar

class CalendarPickerDialogFragment : DialogFragment() {

    companion object {
        const val REQ_KEY = "calendar_pick_req"
        const val BUNDLE_KEY_DATE = "picked_date" // "yyyy-MM-dd"
    }

    private var _binding: DialogCalendarPickerBinding? = null
    private val binding get() = _binding!!

    private val calendar: Calendar = Calendar.getInstance() // 현재 표시 월
    private val today: Calendar = Calendar.getInstance() // 오늘 비교용

    private lateinit var calendarAdapter: CalendarAdapter
    private var selectedDay: Day? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CalendarDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogCalendarPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 요일
        binding.rvWeeklist.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.rvWeeklist.adapter = WeekAdapter(listOf("일", "월", "화", "수", "목", "금", "토"))

        // 달력
        binding.rvCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarAdapter = CalendarAdapter(emptyList()) { day ->
            selectedDay = day
        }
        binding.rvCalendar.adapter = calendarAdapter

        // 초기 월
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        renderMonth()

        binding.btnPrev.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            renderMonth()
        }

        binding.btnNext.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            renderMonth()
        }

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnConfirm.setOnClickListener {
            val sel = selectedDay ?: run {
                // 선택 안 했으면 "오늘"을 기본으로(원하면 그냥 return)
                val y = today.get(Calendar.YEAR)
                val m0 = today.get(Calendar.MONTH)
                val d = today.get(Calendar.DAY_OF_MONTH)
                val dateStr = "%04d\n%02d-%02d".format(y, m0 + 1, d)
                setFragmentResult(REQ_KEY, Bundle().apply { putString(BUNDLE_KEY_DATE, dateStr) })
                dismiss()
                return@setOnClickListener
            }

            val dateStr = "%04d\n%02d-%02d".format(sel.year, sel.month0 + 1, sel.dayOfMonth)
            setFragmentResult(REQ_KEY, Bundle().apply { putString(BUNDLE_KEY_DATE, dateStr) })
            dismiss()
        }
    }

    private fun renderMonth() {
        val year = calendar.get(Calendar.YEAR)
        val month0 = calendar.get(Calendar.MONTH)
        binding.tvMonth.text = "%04d년 %02d월".format(year, month0 + 1)

        val days = build42Days(year, month0)
        calendarAdapter.submitList(days)

        // 기본 선택: 오늘이 같은 월이면 오늘 선택 표시
        if (today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month0) {
            val todayDay = today.get(Calendar.DAY_OF_MONTH)
            val found = days.firstOrNull { it.inMonth && it.dayOfMonth == todayDay }
            if (found != null) {
                selectedDay = found
                calendarAdapter.setSelected(found)
            }
        } else {
            selectedDay = null
        }
    }

    private fun build42Days(year: Int, month0: Int): List<Day> {
        val list = mutableListOf<Day>()

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month0)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDow = cal.get(Calendar.DAY_OF_WEEK) // 1(일)~7(토)
        val thisLast = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 이전 달 정보
        val prev = cal.clone() as Calendar
        prev.add(Calendar.MONTH, -1)
        val prevLast = prev.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 1) 이전달 채우기(첫 요일까지)
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

        // 2) 이번달 채우기
        for (d in 1..thisLast) {
            val c = Calendar.getInstance()
            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, month0)
            c.set(Calendar.DAY_OF_MONTH, d)
            list.add(makeDay(c, inMonth = true))
        }

        // 3) 다음달 채우기(총 42칸)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
