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
import com.bumptech.glide.load.engine.GlideException
import com.google.android.material.snackbar.Snackbar
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.DialogCalendarPickerBinding
import com.ssafy.closetory.databinding.FragmentHomeBinding
import com.ssafy.closetory.dto.StylingResponse
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.homeActivity.home.ImagePreviewActivity
import com.ssafy.closetory.homeActivity.adapter.HomeCalendarAdapter
import com.ssafy.closetory.homeActivity.aiStyling.Day
import com.ssafy.closetory.homeActivity.aiStyling.WeekAdapter
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

    // yyyy-MM-dd
    private var dayColorMap: Map<String, Pair<Int?, Int?>> = emptyMap()
    private var dayImageMap: Map<String, String> = emptyMap()


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
                val imageUrl = dayImageMap[dateKey] // 서버에서 받은 url(있을 수도/없을 수도)
                Log.d(TAG, "selected date=$dateKey, imageUrl=$imageUrl")

                homeCalendarAdapter.setSelected(day)

                if (imageUrl.isNullOrBlank()) {
                    showNoLookMessage()
                    return@HomeCalendarAdapter
                }

                // url이 있는 경우만 화면 이동
                openImagePreview(imageUrl)
            },
            colorProvider = { day ->
                dayColorMap[keyOf(day)] ?: (null to null)
            }
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
            dayColorMap = buildDayColorMap(list)
            dayImageMap = buildDayImageMap(list)
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

    private fun buildDayImageMap(list: List<StylingResponse>): Map<String, String> = list.asSequence()
        .mapNotNull { item ->
            val key = normalizeDateKey(item.date) ?: return@mapNotNull null
            val raw = item.photoUrl.trim()
            if (raw.isBlank()) return@mapNotNull null
            val url = resolveImageUrl(raw)
            key to url
        }
        .toMap()

    private fun resolveImageUrl(raw: String): String {
        if (raw.startsWith("http")) return raw
        val clean = raw.removePrefix("/")
        return "${ApplicationClass.API_BASE_URL}$clean"
    }

    private fun buildDayColorMap(list: List<StylingResponse>): Map<String, Pair<Int?, Int?>> = list.asSequence()
        .mapNotNull { item ->
            val key = normalizeDateKey(item.date) ?: return@mapNotNull null
            key to item
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, items) ->
            val last = items.lastOrNull()
            val top = ColorOptions.englishToArgb(last?.topColor)
            val bottom = ColorOptions.englishToArgb(last?.bottomColor)
            top to bottom
        }

    private fun keyOf(d: Day): String = "%04d-%02d-%02d".format(d.year, d.month0 + 1, d.dayOfMonth)

    private fun normalizeDateKey(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val s = raw.trim()

        if (s.length >= 10 && s[4] == '-' && s[7] == '-') return s.substring(0, 10)

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
