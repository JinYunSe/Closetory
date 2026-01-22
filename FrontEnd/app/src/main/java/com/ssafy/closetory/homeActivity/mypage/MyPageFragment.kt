package com.ssafy.closetory.homeActivity.mypage

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.ssafy.closetory.R
import com.ssafy.closetory.authActivity.AuthActivity
import com.ssafy.closetory.authActivity.logout.LogoutViewModel
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentMyPageBinding
import com.ssafy.closetory.util.AuthManager

class MyPageFragment : BaseFragment<FragmentMyPageBinding>(FragmentMyPageBinding::bind, R.layout.fragment_my_page) {

    private val logoutViewModel: LogoutViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
        observeLogout()
        observeMessage()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("로그아웃")
            .setMessage("정말 로그아웃 하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                requestLogout()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun requestLogout() {
        val authManager = AuthManager(requireContext())
        val token = authManager.getAccessToken() ?: return

        logoutViewModel.logout(
            accessToken = "Bearer $token"
        )
    }

    private fun observeLogout() {
        logoutViewModel.logoutSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                val authManager = AuthManager(requireContext())
                authManager.clearToken()

                val intent = Intent(requireContext(), AuthActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun observeMessage() {
        logoutViewModel.message.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                showToast(it)
            }
        }
    }
}
