// MyPageFragment
package com.ssafy.closetory.homeActivity.myPage

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
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
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.authActivity.AuthActivity
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentMyPageBinding
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.homeActivity.mypage.MyPageViewModel
import com.ssafy.closetory.util.auth.AuthManager
import kotlinx.coroutines.launch

private const val TAG = "MyPageFragment_싸피"

// 마이페이지 Fragment
class MyPageFragment :
    BaseFragment<FragmentMyPageBinding>(
        FragmentMyPageBinding::bind,
        R.layout.fragment_my_page
    ) {

    // 마이페이지 ViewModel
    private val myPageViewModel: MyPageViewModel by viewModels()

    // 비밀번호 확인 다이얼로그 변수
    private var passwordDialog: AlertDialog? = null

    private lateinit var editProfileInfoResponse: EditProfileInfoResponse

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeUserProfile()

        loadUserProfile()

        // 로그아웃 버튼 클릭 이벤트 등록
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        // 회원정보 수정 진입 전 비밀번호 확인 이벤트 등록
        binding.tvEditProfile.setOnClickListener {
            showPasswordCheckDialog()
        }

        // 로그아웃 결과 관찰
        observeLogout()

        // 공통 메시지 관찰 (토스트 출력)
        observeMessage()

        // 비밀번호 검증 결과 관찰
        observePasswordCheck()
    }

    // 서버에 기존 유저 정보 요청
    private fun loadUserProfile() {
        Log.d(TAG, "loadUserProfile: loadUserProfile 실행")
        val userId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID) ?: return
        myPageViewModel.loadUserProfile(userId)
    }

    // 회정 정보 성공 여부 관찰
    private fun observeUserProfile() {
        // 회원정보 수신
        viewLifecycleOwner.lifecycleScope.launch {
            myPageViewModel.userProfile.collect { user ->
                editProfileInfoResponse = user
                Log.d(TAG, "userProfile = $user")
            }
        }
    }

    // 로그아웃 확인 다이얼로그 표시
    private fun showLogoutDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("로그아웃")
            .setMessage("정말 로그아웃 하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                requestLogout()
            }
            .setNegativeButton("취소", null)
            .show()

        // 확인 버튼 색상 설정
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(requireContext().getColor(R.color.main_color))

        // 취소 버튼 색상 설정
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(requireContext().getColor(R.color.gray_500))
    }

    // 로그아웃 요청을 ViewModel에 전달
    private fun requestLogout() {
        myPageViewModel.logout()
    }

    // 로그아웃 성공 여부 관찰
    private fun observeLogout() {
        viewLifecycleOwner.lifecycleScope.launch {
            myPageViewModel.logoutSuccess.collect { success ->
                if (success) {
                    // 저장된 토큰 삭제
                    val authManager = AuthManager(requireContext())
                    authManager.clearToken()
                    ApplicationClass.sharedPreferences.clearUserId(ApplicationClass.USERID)

                    Log.d(
                        TAG,
                        "로그아웃 직후 userId : ${ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID)}"
                    )

                    // 인증 화면으로 이동 및 백스택 제거
                    val intent = Intent(requireContext(), AuthActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }
    }

    // ViewModel에서 전달되는 메시지를 토스트로 출력
    private fun observeMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                myPageViewModel.message.collect { showToast(it) }
            }
        }
    }

    // 회원정보 수정 전 비밀번호 확인 다이얼로그 표시
    private fun showPasswordCheckDialog() {
        // 커스텀 다이얼로그 레이아웃 inflate
        val dialogView = layoutInflater.inflate(R.layout.dialog_password_check, null)

        // 다이얼로그 내부 뷰 참조
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val btnToggle = dialogView.findViewById<ImageButton>(R.id.btnTogglePassword)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        // 비밀번호 표시 여부 상태값
        var visible = false

        // 비밀번호 표시 토글 버튼 클릭 이벤트
        btnToggle.setOnClickListener {
            visible = togglePasswordVisibility(etPassword, visible)
        }

        // 다이얼로그 생성
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // 다이얼로그 참조 저장
        passwordDialog = dialog

        // 취소 버튼 클릭 시 다이얼로그 종료
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // 확인 버튼 클릭 시 비밀번호 검증 요청
        btnConfirm.setOnClickListener {
            val password = etPassword.text.toString()

            // 입력값 검증
            if (password.isBlank()) {
                showToast("비밀번호를 입력해주세요.")
                return@setOnClickListener
            }

            // ViewModel에 비밀번호 검증 요청
            myPageViewModel.checkPassword(password)
        }

        // 다이얼로그 표시
        dialog.show()
    }

    // 비밀번호 검증 결과 관찰
    private fun observePasswordCheck() {
        // 비밀번호 검증 성공 여부 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            myPageViewModel.passwordVerified.collect { success ->

                if (success) {
                    // 다이얼로그 종료
                    passwordDialog?.dismiss()
                    passwordDialog = null

                    // 회원정보 수정 화면으로 이동
                    findNavController()
                        .navigate(R.id.action_navigation_my_page_to_editProfileFragment)
                }
            }
        }

        // 비밀번호 검증 관련 메시지 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            myPageViewModel.message.collect { msg ->
                showToast(msg)
            }
        }
    }

    // 비밀번호 입력창 표시 및 숨김 처리
    private fun togglePasswordVisibility(editText: EditText, isVisible: Boolean): Boolean {
        editText.inputType =
            if (isVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }

        // 커서 위치 유지
        editText.setSelection(editText.text.length)
        return !isVisible
    }
}
