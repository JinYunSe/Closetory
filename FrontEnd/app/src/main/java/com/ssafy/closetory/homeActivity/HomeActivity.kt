package com.ssafy.closetory.homeActivity

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseActivity
import com.ssafy.closetory.databinding.ActivityHomeBinding
import com.ssafy.closetory.homeActivity.mypage.MyPageViewModel
import com.ssafy.closetory.util.TagOptions
import kotlinx.coroutines.launch

private const val TAG = "HomeActivity_싸피"
class HomeActivity : BaseActivity<ActivityHomeBinding>(ActivityHomeBinding::inflate) {

    private data class NavItem(val destinationId: Int, val containerId: Int, val iconId: Int, val textId: Int)

    private val homeInitViewModel: HomeInitViewModel by viewModels()

    private val myPageViewModel: MyPageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()

        // 하단바 아이템과 네비게이션 목적지를 연결한다.
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val items = listOf(
            NavItem(
                destinationId = R.id.navigation_home,
                containerId = R.id.nav_item_home,
                iconId = R.id.nav_icon_home,
                textId = R.id.nav_text_home
            ),
            NavItem(
                destinationId = R.id.navigation_closet,
                containerId = R.id.nav_item_closet,
                iconId = R.id.nav_icon_closet,
                textId = R.id.nav_text_closet
            ),
            NavItem(
                destinationId = R.id.navigation_ai_styling,
                containerId = R.id.nav_item_styling,
                iconId = R.id.nav_icon_styling,
                textId = R.id.nav_text_styling
            ),
            NavItem(
                destinationId = R.id.navigation_my_page,
                containerId = R.id.nav_item_my_page,
                iconId = R.id.nav_icon_my_page,
                textId = R.id.nav_text_my_page
            )
        )

        items.forEach { item ->
            binding.root.findViewById<android.view.View>(item.containerId).setOnClickListener {
                navigateTo(navController, item.destinationId)
            }
        }

        // FAB 클릭 시 AddClose 화면으로 이동한다.
        binding.fabAdd.setOnClickListener {
            // 온보딩 페이지에서는 FAB 먹통
            if (navController.currentDestination?.id == R.id.navigation_tag_onboarding) return@setOnClickListener
            navigateTo(navController, R.id.navigation_registration_clothes)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val active = items.firstOrNull { it.destinationId == destination.id }
            updateSelection(items, active)
        }

        // 앱 시작 시 기본 선택 상태를 적용한다.
        updateSelection(items, items.first())

        // 홈 진입 시 온보딩 자동 표시 (1회만)
        if (savedInstanceState == null) {
            val sp = com.ssafy.closetory.baseCode.data.local.SharedPreferencesUtil(this)
            val userId = sp.getUserId(com.ssafy.closetory.ApplicationClass.USERID, -1) ?: -1

            // 로그인 100% 보장이라고 해도, -1 방어는 해두는 게 안전
            if (userId != -1 && !sp.isOnboardingDone(userId)) {
                navController.navigate(
                    R.id.navigation_tag_onboarding,
                    null,
                    navOptions {
                        // 현재 그래프의 startDestination(보통 navigation_home)을 백스택에서 제거
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                )
            }
        }

        registerObserve()
    }

    private fun updateSelection(items: List<NavItem>, active: NavItem?) {
        val activeAlpha = 1f
        val inactiveAlpha = 0.6f

        items.forEach { item ->
            val isActive = active?.destinationId == item.destinationId
            val iconView = binding.root.findViewById<android.view.View>(item.iconId)
            iconView.isSelected = isActive
            iconView.alpha = if (isActive) activeAlpha else inactiveAlpha
            binding.root.findViewById<android.view.View>(item.textId).alpha =
                if (isActive) activeAlpha else inactiveAlpha
        }
    }

    private fun navigateTo(navController: androidx.navigation.NavController, destinationId: Int) {
        // 온보딩 화면이면 하단바 탭 이동 먹통
        if (navController.currentDestination?.id == R.id.navigation_tag_onboarding) return

        if (navController.currentDestination?.id == destinationId) {
            return
        }
        navController.navigate(destinationId)
    }

    private fun init() {
        homeInitViewModel.getTagsList()

        val userId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID)!!
        Log.d(TAG, "로그인 직후 userId 가져오기 : $userId")
        myPageViewModel.loadUserProfile(userId)
    }

    fun registerObserve() {
        homeInitViewModel.tagsList.observe(this) {
            TagOptions.setTags(it)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                myPageViewModel.userProfile.collect { profile ->
                    if (profile == null) return@collect

                    Log.d(TAG, "userProfile 수신: $profile")
                    ApplicationClass.sharedPreferences.putUserNickName(profile.nickname)

                    val userNickName = ApplicationClass.sharedPreferences.getUserNickName()
                    Log.d(TAG, "userNickName: $userNickName")
                }
            }
        }
    }
}
