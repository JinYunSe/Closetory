package com.ssafy.closetory.homeActivity.mypage

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.authActivity.AuthActivity
import com.ssafy.closetory.authActivity.logout.LogoutViewModel
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentMyPageBinding
import com.ssafy.closetory.homeActivity.mypage.signout.SignoutViewModel
import com.ssafy.closetory.util.AuthManager
import kotlinx.coroutines.launch

private const val TAG = "MyPageFragment_싸피"

class MyPageFragment :
    BaseFragment<FragmentMyPageBinding>(
        FragmentMyPageBinding::bind,
        R.layout.fragment_my_page
    ) {

    private val logoutViewModel: LogoutViewModel by viewModels()
    private val signoutViewModel: SignoutViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 로그아웃
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        // 회원 탈퇴
        binding.btnSignout.setOnClickListener {
            Log.d(TAG, "회원 탈퇴 버튼 클릭")
            showSignoutDialog()
        }

        observeLogout()
        collectSignout()
    }

    /* -------------------- 로그아웃 -------------------- */

    private fun showLogoutDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("로그아웃")
            .setMessage("정말 로그아웃 하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                requestLogout()
            }
            .setNegativeButton("취소", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(requireContext().getColor(R.color.main_color))

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(requireContext().getColor(R.color.gray_500))
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
                AuthManager(requireContext()).clearToken()
                moveToLogin()
            }
        }

        logoutViewModel.message.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /* -------------------- 회원 탈퇴 -------------------- */

    private fun collectSignout() {
        viewLifecycleOwner.lifecycleScope.launch {
            signoutViewModel.signoutSuccess.collect {
                AuthManager(requireContext()).clearToken()
                moveToLogin()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            signoutViewModel.message.collect { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSignoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_signout, null)

        val etPassword = dialogView.findViewById<EditText>(R.id.etSignoutPassword)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnSignoutConfirm)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnSignoutCancel)
        val btnToggle = dialogView.findViewById<ImageButton>(R.id.btnToggleSignoutPassword)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        var isPasswordVisible = false

        // 비밀번호 보기 / 숨기기
        btnToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            etPassword.inputType =
                if (isPasswordVisible) {
                    InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                } else {
                    InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
                }

            etPassword.setSelection(etPassword.text.length)
        }

        // 탈퇴 확인
        btnConfirm.setOnClickListener {
            val password = etPassword.text.toString()

            if (password.isBlank()) {
                Toast.makeText(requireContext(), "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = ApplicationClass.sharedPreferences
                .getUserId(ApplicationClass.USERID)
                ?: return@setOnClickListener

            signoutViewModel.signout(userId, password)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /* -------------------- 공통 -------------------- */

    private fun moveToLogin() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
