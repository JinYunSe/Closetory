package com.ssafy.closetory.homeActivity.home

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
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
import kotlinx.coroutines.launch

private const val TAG = "HomeFragment_싸피"

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::bind, R.layout.fragment_home) {

    // 달력 현재 표시 월
    private val calendar: Calendar = Calendar.getInstance()

    // 오늘 비교용
    private val today: Calendar = Calendar.getInstance()

    private lateinit var calBinding: DialogCalendarPickerBinding
    private lateinit var homeCalendarAdapter: HomeCalendarAdapter
    private var selectedDay: Day? = null

    private lateinit var homeActivity: HomeActivity
    private val homeViewModel: HomeViewModel by viewModels()

    // 날짜별 상의색, 하의색 (키: yyyy-MM-dd)
    private var dayColorMap: Map<String, Pair<Int?, Int?>> = emptyMap()

    // 날짜별 코디 이미지 URL (키: yyyy-MM-dd)
    private var dayImageMap: Map<String, String> = emptyMap()

    // 날짜 선택 다이얼로그 중복 방지
    private var selectedDateDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 월별 데이터 로드
        homeViewModel.getStylingList(true)

        homeActivity = requireContext() as HomeActivity

        setupClickPostBtn()
        setupEmbeddedCalendar(view)

        observeStylingList()
        collectMessageEvent()
    }

    /** 게시글 리스트 화면으로 이동 */
    private fun setupClickPostBtn() {
        binding.btnPosts.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_post_list)
        }
    }

    /** 홈에 include된 캘린더 뷰 바인딩 + 초기 렌더 */
    private fun setupEmbeddedCalendar(root: View) {
        bindCalendarInclude(root)
        setupWeekHeader()
        setupCalendarGrid()

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        renderMonth()

        setupMonthNavButtons()

        // 재사용 XML에서 불필요 요소 숨김
        calBinding.btnConfirm.visibility = View.GONE
        calBinding.btnCancel.visibility = View.GONE
        calBinding.tvTitle.visibility = View.GONE
    }

    /** include 루트를 찾아 DialogCalendarPickerBinding으로 바인딩 */
    private fun bindCalendarInclude(root: View) {
        val includeRoot = root.findViewById<View>(R.id.home_calender)
        calBinding = DialogCalendarPickerBinding.bind(includeRoot)
    }

    /** 요일(일~토) 고정 헤더 세팅 */
    private fun setupWeekHeader() {
        calBinding.rvWeeklist.layoutManager = GridLayoutManager(homeActivity, 7)
        calBinding.rvWeeklist.adapter = WeekAdapter(listOf("일", "월", "화", "수", "목", "금", "토"))
    }

    /** 달력(42칸) 어댑터 세팅 + 날짜 클릭 처리(다이얼로그 표시) */
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

    /** 이전/다음 월 이동 버튼 세팅 (서버 재호출 없이 로컬 렌더만) */
    private fun setupMonthNavButtons() {
        calBinding.btnPrev.setOnClickListener { moveMonthAndRender(-1) }
        calBinding.btnNext.setOnClickListener { moveMonthAndRender(1) }
    }

    /** 월 이동 후 캘린더만 다시 렌더 (API가 연/월을 안 받으므로 서버 재호출 X) */
    private fun moveMonthAndRender(deltaMonth: Int) {
        calendar.add(Calendar.MONTH, deltaMonth)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        renderMonth()
    }

    /** 서버에서 받은 stylingList를 observe → 날짜별 색/이미지 맵 갱신 후 재렌더 */
    private fun observeStylingList() {
        homeViewModel.stylingList.observe(viewLifecycleOwner) { list ->
            dayColorMap = buildDayColorMap(list)
            dayImageMap = buildDayImageMap(list)
            renderMonth()
        }
    }

    /** ViewModel의 1회성 메시지(토스트/스낵바용) 수집 */
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

    /** 서버 리스트 → 날짜별 이미지 URL 맵 생성 (키를 yyyy-MM-dd로 통일) */
    private fun buildDayImageMap(list: List<StylingResponse>): Map<String, String> = list.asSequence()
        .mapNotNull { item ->
            val key = normalizeDateKey(item.date) ?: return@mapNotNull null
            val url = item.photoUrl?.trim()
            if (url.isNullOrBlank()) null else key to url
        }
        .toMap()

    /** 월 표시 텍스트/42칸 리스트 갱신 + 오늘 자동 선택 */
    private fun renderMonth() {
        val year = calendar.get(Calendar.YEAR)
        val month0 = calendar.get(Calendar.MONTH)

        calBinding.tvMonth.text = "%04d년 %02d월".format(year, month0 + 1)

        val days = build42Days(year, month0)
        homeCalendarAdapter.submitList(days)

        applyDefaultSelectionIfTodayInThisMonth(year, month0, days)
    }

    /** 오늘이 현재 표시 월이면 오늘 날짜를 선택 상태로 표시 */
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

    /** (42칸) 이전달/이번달/다음달 포함 Day 리스트 생성 */
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

    /** Calendar → Day 변환 */
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

    /** 서버 리스트 → 날짜별 (상의색, 하의색) ARGB 맵 생성 (키를 yyyy-MM-dd로 통일) */
    private fun buildDayColorMap(list: List<StylingResponse>): Map<String, Pair<Int?, Int?>> = list.asSequence()
        .mapNotNull { item ->
            val key = normalizeDateKey(item.date) ?: return@mapNotNull null
            key to item
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, items) ->
            val item = items.lastOrNull()
            val top = ColorOptions.englishToArgb(item?.topColor)
            val bottom = ColorOptions.englishToArgb(item?.bottomColor)
            top to bottom
        }

    /** Day를 yyyy-MM-dd 키로 변환 */
    private fun keyOf(d: Day): String {
        val m = d.month0 + 1
        return "%04d-%02d-%02d".format(d.year, m, d.dayOfMonth)
    }

    /** 날짜 클릭 시: 해당 날짜 이미지가 있으면 다이얼로그 ImageView에 로드 */
    private fun showSelectedDateDialog(day: Day) {
        selectedDateDialog?.dismiss()
        selectedDateDialog = null

        val dialogView = layoutInflater.inflate(R.layout.dialog_main_calendar_selected_date, null)
        val ivClose = dialogView.findViewById<ImageView>(R.id.iv_home_fragment_close)
        val ivCoord = dialogView.findViewById<ImageView>(R.id.iv_home_fragment_coordination)

        val dateKey = formatDay(day)

        logSelectedDate(dateKey)

        val imageUrl = dayImageMap[dateKey]
        bindCoordinationImage(ivCoord, imageUrl)

        val dialog = AlertDialog.Builder(homeActivity)
            .setView(dialogView)
            .create()

        ivClose.setOnClickListener { dialog.dismiss() }

        dialog.setOnDismissListener {
            if (selectedDateDialog === dialog) selectedDateDialog = null
        }

        selectedDateDialog = dialog
        dialog.show()
    }

    /** 선택 날짜 이미지 바인딩 (없으면 기본 이미지) + 성공/실패 로그 */
    private fun bindCoordinationImage(target: ImageView, imageUrl: String?) {
        if (imageUrl.isNullOrBlank()) {
            target.setImageResource(R.drawable.iron_man)
            Log.d(TAG, "bindCoordinationImage: imageUrl is null/blank")
            return
        }

        Glide.with(this@HomeFragment)
            .load(imageUrl)
            .placeholder(R.drawable.iron_man)
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.iron_man)
            .listener(object : RequestListener<android.graphics.drawable.Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e(TAG, "Glide load failed. url=$imageUrl, e=$e")
                    e?.rootCauses?.forEach { cause -> Log.e(TAG, "rootCause: $cause") }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d(TAG, "Glide success. url=$imageUrl, source=$dataSource")
                    return false
                }
            })
            .into(target)
    }

    private fun logSelectedDate(dateKey: String) {
        Log.d(TAG, "showSelectedDateDialog:")
        Log.d(TAG, "clicked dateKey=$dateKey")
        Log.d(TAG, "dayImageMap keys sample=${dayImageMap.keys.take(5)}")
        Log.d(TAG, "imageUrl=${dayImageMap[dateKey]}")
    }

    override fun onDestroyView() {
        selectedDateDialog?.dismiss()
        selectedDateDialog = null
        super.onDestroyView()
    }

    /** Day → yyyy-MM-dd */
    private fun formatDay(day: Day): String = "%04d-%02d-%02d".format(day.year, day.month0 + 1, day.dayOfMonth)

    /** 서버 date → yyyy-MM-dd (HomeFragment 내부 전용) */
    private fun normalizeDateKey(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val s = raw.trim()

        // ISO / datetime 형태면 앞 10자리만
        if (s.length >= 10 && s[4] == '-' && s[7] == '-') return s.substring(0, 10)

        // yyyy-M-d 같은 변형 대응
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
