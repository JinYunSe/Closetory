package com.ssafy.closetory.homeActivity

import android.os.Bundle
import androidx.navigation.findNavController
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseActivity
import com.ssafy.closetory.databinding.ActivityHomeBinding
import com.ssafy.closetory.util.PermissionChecker

class HomeActivity : BaseActivity<ActivityHomeBinding>(ActivityHomeBinding::inflate) {

    private data class NavItem(val destinationId: Int, val containerId: Int, val iconId: Int, val textId: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                destinationId = R.id.navigation_styling,
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
        binding.fabAdd.setOnClickListener { navigateTo(navController, R.id.navigation_add_close) }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val active = items.firstOrNull { it.destinationId == destination.id }
            updateSelection(items, active)
        }
        // 앱 시작 시 기본 선택 상태를 적용한다.
        updateSelection(items, items.first())
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
        if (navController.currentDestination?.id == destinationId) {
            return
        }
        navController.navigate(destinationId)
    }
}
