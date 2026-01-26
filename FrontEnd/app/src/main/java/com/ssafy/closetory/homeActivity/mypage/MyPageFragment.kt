package com.ssafy.closetory.homeActivity.mypage

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ssafy.closetory.R
import com.ssafy.closetory.authActivity.AuthActivity
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentMyPageBinding
import com.ssafy.closetory.util.auth.AuthManager
import kotlinx.coroutines.launch

class MyPageFragment : BaseFragment<FragmentMyPageBinding>(FragmentMyPageBinding::bind, R.layout.fragment_my_page) {

    private val myPageViewModel: MyPageViewModel by viewModels()

    private var passwordDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 로그아웃 버튼 이벤트
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        // 회원정보 수정 진입 전 비밀번호 확인 이벤트
        binding.tvEditProfile.setOnClickListener {
            showPasswordCheckDialog()
        }

        observeLogout()
        observeMessage()
        observePasswordCheck()
    }

    // 로그아웃 다이얼로그
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

    // 로그아웃 요청
    private fun requestLogout() {
        myPageViewModel.logout()
    }

    // 로그아웃 옵저버
    private fun observeLogout() {
        viewLifecycleOwner.lifecycleScope.launch {
            myPageViewModel.logoutSuccess.collect { success ->
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
    }

    private fun observeMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                myPageViewModel.message.collect { showToast(it) }
            }
        }
    }

    // 비밀번호 확인 다이얼로그
    private fun showPasswordCheckDialog() {
        val dialogView =
            layoutInflater.inflate(R.layout.dialog_password_check, null)

        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val btnToggle = dialogView.findViewById<ImageButton>(R.id.btnTogglePassword)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        var visible = false

        btnToggle.setOnClickListener {
            visible = togglePasswordVisibility(etPassword, visible)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        passwordDialog = dialog

        // 비밀번호 확인의 취소 버튼
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // 비밀번호 확인의 확인 버튼
        btnConfirm.setOnClickListener {
            val password = etPassword.text.toString()

            // 사용자 입력 검증 (Fragment에서)
            if (password.isBlank()) {
                showToast("비밀번호를 입력해주세요.")
                return@setOnClickListener
            }

            myPageViewModel.checkPassword(password)
        }

        dialog.show()
    }

    private fun observePasswordCheck() {
        viewLifecycleOwner.lifecycleScope.launch {
            myPageViewModel.passwordVerified.collect { success ->
                if (success) {
                    passwordDialog?.dismiss()
                    passwordDialog = null

                    findNavController()
                        .navigate(R.id.action_navigation_my_page_to_editProfileFragment)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            myPageViewModel.message.collect { msg ->
                showToast(msg)
            }
        }
    }

    // 비밀번호 표시/숨김 토글
    private fun togglePasswordVisibility(editText: EditText, isVisible: Boolean): Boolean {
        editText.inputType =
            if (isVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }

        editText.setSelection(editText.text.length)
        return !isVisible
    }
}
