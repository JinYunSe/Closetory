package com.ssafy.closetory.homeActivity.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.DialogCalendarPickerBinding
import com.ssafy.closetory.databinding.FragmentHomeBinding
import com.ssafy.closetory.util.CalendarAdapter
import com.ssafy.closetory.util.Day
import com.ssafy.closetory.util.WeekAdapter
import java.util.Calendar

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::bind, R.layout.fragment_home) {

    // 캘린더(내장용)
    private val calendar: Calendar = Calendar.getInstance() // 현재 표시 월
    private val today: Calendar = Calendar.getInstance() // 오늘 비교용

    private lateinit var calBinding: DialogCalendarPickerBinding
    private lateinit var calendarAdapter: CalendarAdapter
    private var selectedDay: Day? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 게시글 더보기
        setupClickPostBtn()

        // 홈에 캘린더 내장
        setupEmbeddedCalendar(view)
    }

    private fun setupClickPostBtn() {
        binding.btnPosts.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_post_list)
        }
    }

    private fun setupEmbeddedCalendar(root: View) {
        // 1) include된 dialog_calendar_picker.xml 루트를 찾아서 Binding
        val includeRoot = root.findViewById<View>(R.id.home_calender)
        calBinding = DialogCalendarPickerBinding.bind(includeRoot)

        // 2) 요일(일~토)
        calBinding.rvWeeklist.layoutManager = GridLayoutManager(requireContext(), 7)
        calBinding.rvWeeklist.adapter = WeekAdapter(listOf("일", "월", "화", "수", "목", "금", "토"))

        // 3) 달력(42칸)
        calBinding.rvCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarAdapter = CalendarAdapter(emptyList()) { day ->
            selectedDay = day
        }
        calBinding.rvCalendar.adapter = calendarAdapter

        // 4) 초기 월 렌더
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        renderMonth()

        // 5) 월 이동
        calBinding.btnPrev.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            renderMonth()
        }

        calBinding.btnNext.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            renderMonth()
        }

        // 6) Dialog 버튼을 “홈 내장”에 맞게 재정의
        calBinding.btnCancel.setOnClickListener {
            // 선택 초기화(원하면 버튼 자체를 GONE 처리해도 됨)
            selectedDay = null
            renderMonth()
        }

        calBinding.btnConfirm.setOnClickListener {
            // 선택 없으면 오늘로 처리
            val sel = selectedDay ?: run {
                val y = today.get(Calendar.YEAR)
                val m0 = today.get(Calendar.MONTH)
                val d = today.get(Calendar.DAY_OF_MONTH)
                val dateStr = "%04d-%02d-%02d".format(y, m0 + 1, d)

                // TODO: 홈에서 어디에 쓸지에 따라 처리

                // 예) binding.tvPickedDate.text = dateStr
                return@setOnClickListener
            }

            val dateStr = "%04d-%02d-%02d".format(sel.year, sel.month0 + 1, sel.dayOfMonth)
            // TODO: 홈에서 어디에 쓸지에 따라 처리
            // 예) binding.tvPickedDate.text = dateStr
        }
    }

    private fun renderMonth() {
        val year = calendar.get(Calendar.YEAR)
        val month0 = calendar.get(Calendar.MONTH)

        // dialog_calendar_picker.xml의 tvMonth 사용
        calBinding.tvMonth.text = "%04d년 %02d월".format(year, month0 + 1)

        val days = build42Days(year, month0)
        calendarAdapter.submitList(days)

        // 기본 선택: 오늘이 같은 월이면 오늘 자동 선택 표시
        if (today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month0) {
            val todayDay = today.get(Calendar.DAY_OF_MONTH)
            val found = days.firstOrNull { it.inMonth && it.dayOfMonth == todayDay }
            if (found != null) {
                selectedDay = found
//                selectedKey = keyof(found)
                calendarAdapter.setSelected(found)
            }
        } else {
            selectedDay = null
//            selectedKey = null
        }
    }

    // 아래 2개는 CalendarPickerDialogFragment의 로직 그대로(42칸 생성)
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

        // 1) 이전달 채우기
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

    private fun showSelectedDateDialog(day: Day) {
        val dateStr = "%04d-%02d-%02d".format(day.year, day.month0 + 1, day.dayOfMonth)

        AlertDialog.Builder(requireContext())
            .setTitle("선택한 날짜")
            .setMessage(dateStr)
            .setPositiveButton("확인", null)
            .show()
    }
}
