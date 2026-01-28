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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.authActivity.AuthActivity
import com.ssafy.closetory.authActivity.logout.MyPageViewModel
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentMyPageBinding
import com.ssafy.closetory.homeActivity.mypage.signout.SignoutViewModel
import com.ssafy.closetory.util.auth.AuthManager
import kotlinx.coroutines.launch

private const val TAG = "MyPageFragment_싸피"

class MyPageFragment :
    BaseFragment<FragmentMyPageBinding>(
        FragmentMyPageBinding::bind,
        R.layout.fragment_my_page
    ) {

    private val homeViewModel: MyPageViewModel by viewModels()
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
        observeMessage()
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

        // 버튼 색상 변경
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(requireContext().getColor(R.color.main_color))

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(requireContext().getColor(R.color.gray_500))
    }

    private fun requestLogout() {
        homeViewModel.logout()
    }

    private fun observeLogout() {
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.logoutSuccess.collect { success ->
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
                homeViewModel.message.collect { showToast(it) }
            }
        }
    }

/* -------------------- 회원 탈퇴 -------------------- */

    private fun collectSignout() {
        viewLifecycleOwner.lifecycleScope.launch {
            signoutViewModel.signoutSuccess.collect {
                ApplicationClass.authManager.clearToken()
                ApplicationClass.sharedPreferences.clearUserId(ApplicationClass.USERID)

                moveToLogin()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            signoutViewModel.message.collect { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 회원 탈퇴 다이얼로그 띄우기
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
            Log.d(
                TAG,
                "USERID key = '${ApplicationClass.USERID}', PREF_NAME='${ApplicationClass.SHARED_PREFERENCES_NAME}'"
            )

            Log.d(TAG, "userId value = ${ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID)}")

            // SharedPreferencesUtil 수정 안 하므로 null 체크가 아니라 -1 체크해야 함
            val userId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID) ?: -1
            Log.d(TAG, "userID : $userId")
            if (userId == -1) {
                Toast.makeText(requireContext(), "유저 정보가 없습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
