package com.ssafy.closetory.homeActivity.home

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.DialogCalendarPickerBinding
import com.ssafy.closetory.databinding.FragmentHomeBinding
import com.ssafy.closetory.dto.StylingResponse
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.homeActivity.adapter.HomeCalendarAdapter
import com.ssafy.closetory.homeActivity.aiStyling.Day
import com.ssafy.closetory.homeActivity.aiStyling.WeekAdapter
import com.ssafy.closetory.homeActivity.home.ImagePreviewActivity
import com.ssafy.closetory.util.ColorOptions
import java.util.Calendar
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::bind, R.layout.fragment_home) {

    companion object {
        private const val TAG = "HomeFragment"
    }

    private val calendar: Calendar = Calendar.getInstance()
    private val today: Calendar = Calendar.getInstance()

    private lateinit var calBinding: DialogCalendarPickerBinding
    private lateinit var homeCalendarAdapter: HomeCalendarAdapter
    private lateinit var homeActivity: HomeActivity

    private val homeViewModel: HomeViewModel by viewModels()

    // yyyy-MM-dd -> (topColorARGB, bottomColorARGB)
    private var dayColorMap: Map<String, Pair<Int?, Int?>> = emptyMap()

    // yyyy-MM-dd -> photoUrl
    private var dayImageMap: Map<String, String> = emptyMap()

    // 등록된 날짜 Set
    private var registeredDates: Set<String> = emptySet()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireContext() as HomeActivity
        homeViewModel.getStylingList(true)

        setupClickPostBtn()
        setupEmbeddedCalendar(view)

        observeStylingList()
        collectMessageEvent()
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

        calBinding.btnPrev.setOnClickListener { moveMonthAndRender(-1) }
        calBinding.btnNext.setOnClickListener { moveMonthAndRender(1) }

        calBinding.btnConfirm.visibility = View.GONE
        calBinding.btnCancel.visibility = View.GONE
        calBinding.tvTitle.visibility = View.GONE
    }

    private fun bindCalendarInclude(root: View) {
        val includeRoot = root.findViewById<View>(R.id.home_calender)
        calBinding = DialogCalendarPickerBinding.bind(includeRoot)
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
                if (!day.inMonth) return@HomeCalendarAdapter

                val dateKey = keyOf(day)
                val imageUrl = dayImageMap[dateKey]

                Log.d(TAG, "📅 선택된 날짜: $dateKey")
                Log.d(TAG, "🖼️ 이미지 URL: $imageUrl")
                Log.d(TAG, "✅ 등록 여부: ${registeredDates.contains(dateKey)}")

                homeCalendarAdapter.setSelected(day)

                if (imageUrl.isNullOrBlank()) {
                    showNoLookMessage()
                    return@HomeCalendarAdapter
                }

                openImagePreview(imageUrl)
            },
            colorProvider = { day ->
                val dateKey = keyOf(day)
                val colors = dayColorMap[dateKey] ?: (null to null)
                colors
            },
            isBlocked = { _ -> false } // 등록된 날짜도 클릭 가능 (수정/삭제는 나중에)
        )

        calBinding.rvCalendar.adapter = homeCalendarAdapter
    }

    private fun showNoLookMessage() {
        Toast.makeText(requireContext(), "등록된 룩이 없습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun moveMonthAndRender(deltaMonth: Int) {
        calendar.add(Calendar.MONTH, deltaMonth)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        renderMonth()
    }

    private fun observeStylingList() {
        homeViewModel.stylingList.observe(viewLifecycleOwner) { list ->
            Log.d(TAG, "🎨 스타일링 리스트 받음: ${list.size}개")

            dayColorMap = buildDayColorMap(list)
            dayImageMap = buildDayImageMap(list)
            registeredDates = list.mapNotNull { normalizeDateKey(it.date) }.toSet()

            Log.d(TAG, "🗓️ 등록된 날짜: $registeredDates")
            Log.d(TAG, "🎨 색상 맵: $dayColorMap")

            renderMonth()
        }
    }

    private fun collectMessageEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.message.collect { msg ->
                    if (!msg.isNullOrBlank()) {
                        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun renderMonth() {
        val year = calendar.get(Calendar.YEAR)
        val month0 = calendar.get(Calendar.MONTH)
        calBinding.tvMonth.text = "%04d년%02d월".format(year, month0 + 1)

        val days = build42Days(year, month0)
        homeCalendarAdapter.submitList(days)

        if (today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month0) {
            val todayDom = today.get(Calendar.DAY_OF_MONTH)
            days.firstOrNull { it.inMonth && it.dayOfMonth == todayDom }?.let {
                homeCalendarAdapter.setSelected(it)
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

    private fun openImagePreview(imageUrl: String) {
        val intent = android.content.Intent(requireContext(), ImagePreviewActivity::class.java)
        intent.putExtra(ImagePreviewActivity.EXTRA_IMAGE_URL, imageUrl)
        startActivity(intent)
    }

    /**
     * 날짜별 이미지 URL 매핑
     */
    private fun buildDayImageMap(list: List<StylingResponse>): Map<String, String> = list.asSequence()
        .mapNotNull { item ->
            val key = normalizeDateKey(item.date) ?: return@mapNotNull null
            val raw = item.photoUrl.trim()
            if (raw.isBlank()) return@mapNotNull null
            val url = resolveImageUrl(raw)
            key to url
        }
        .toMap()

    /**
     * 이미지 URL 전처리 (상대경로 -> 절대경로)
     */
    private fun resolveImageUrl(raw: String): String {
        if (raw.startsWith("http")) return raw
        val clean = raw.removePrefix("/")
        return "${ApplicationClass.API_BASE_URL}$clean"
    }

    /**
     * 날짜별 색상 매핑 (상의/하의)
     */
    private fun buildDayColorMap(list: List<StylingResponse>): Map<String, Pair<Int?, Int?>> {
        return list.asSequence()
            .mapNotNull { item ->
                val key = normalizeDateKey(item.date) ?: return@mapNotNull null
                key to item
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (dateKey, items) ->
                // 같은 날짜에 여러 룩이 있으면 마지막 것 사용
                val last = items.lastOrNull()
                val topColor = ColorOptions.englishToArgb(last?.topColor)
                val bottomColor = ColorOptions.englishToArgb(last?.bottomColor)

                Log.d(TAG, "🎨 날짜: $dateKey")
                Log.d(TAG, "   - 원본 상의: ${last?.topColor} -> ARGB: $topColor")
                Log.d(TAG, "   - 원본 하의: ${last?.bottomColor} -> ARGB: $bottomColor")

                topColor to bottomColor
            }
    }

    /**
     * Day 객체를 yyyy-MM-dd 형식의 키로 변환
     */
    private fun keyOf(d: Day): String = "%04d-%02d-%02d".format(d.year, d.month0 + 1, d.dayOfMonth)

    /**
     * 서버에서 받은 날짜 문자열을 yyyy-MM-dd 형식으로 정규화
     */
    private fun normalizeDateKey(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val s = raw.trim()

        // 이미 yyyy-MM-dd 형식이면 그대로 반환
        if (s.length >= 10 && s[4] == '-' && s[7] == '-') {
            return s.substring(0, 10)
        }

        // T 또는 공백으로 분리된 경우 처리
        return try {
            val head = s.split("T", " ").first()
            val parts = head.split("-")
            if (parts.size >= 3) {
                val y = parts[0].toInt()
                val m = parts[1].toInt()
                val d = parts[2].toInt()
                "%04d-%02d-%02d".format(y, m, d)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
