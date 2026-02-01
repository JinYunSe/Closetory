package com.ssafy.closetory.homeActivity.myPage

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
import androidx.navigation.fragment.findNavController
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.authActivity.AuthActivity
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentMyPageBinding
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.homeActivity.mypage.MyPageViewModel
import com.ssafy.closetory.homeActivity.mypage.signout.SignoutViewModel
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
    private val signoutViewModel: SignoutViewModel by viewModels()

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

        // 회원 탈퇴
        binding.btnSignout.setOnClickListener {
            Log.d(TAG, "회원 탈퇴 버튼 클릭")
            showSignoutDialog()
        }

        // 회원정보 수정 진입 전 비밀번호 확인 이벤트 등록
        binding.tvEditProfile.setOnClickListener {
            showPasswordCheckDialog()
        }

        // 로그아웃 결과 관찰
        observeLogout()

        // 공통 메시지 관찰 (토스트 출력)
        observeMessage()
        collectSignout()

        // 비밀번호 검증 결과 관찰
        observePasswordCheck()
    }

    // 회원정보를 화면에 바인딩
    private fun bindUserProfile(user: EditProfileInfoResponse) {
        binding.tvNickname.text = user.nickname ?: "닉네임"
        binding.tvHeight.text = "${user.height ?: 0}cm"
        binding.tvWeight.text = "${user.weight ?: 0}kg"

        // 프로필/전신 사진 URL이 있다면 Glide로 로드 (없으면 기본 이미지 유지)
        bindProfileImage(user.profilePhotoUrl)
        bindBodyImage(user.bodyPhotoUrl)
    }

    // 프로필 이미지 바인딩
    private fun bindProfileImage(url: String?) {
        if (url.isNullOrBlank()) {
            binding.ivProfile.setImageResource(R.drawable.ic_profile_default)
            return
        }

        com.bumptech.glide.Glide.with(this)
            .load(url)
            .placeholder(R.drawable.ic_profile_default)
            .error(R.drawable.ic_profile_default)
            .into(binding.ivProfile)
    }

    // 전신 이미지 바인딩
    private fun bindBodyImage(url: String?) {
        if (url.isNullOrBlank()) {
            binding.ivBodyPhoto.setImageResource(R.drawable.ic_body_default)
            return
        }

        com.bumptech.glide.Glide.with(this)
            .load(url)
            .placeholder(R.drawable.ic_body_default)
            .error(R.drawable.ic_body_default)
            .into(binding.ivBodyPhoto)
    }

    // 회원 정보 수정 전 서버에 기존 유저 정보 요청
    private fun loadUserProfile() {
        Log.d(TAG, "loadUserProfile: loadUserProfile 실행")
        val userId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID) ?: return
        myPageViewModel.loadUserProfile(userId)
    }

    /* -------------------- 로그아웃 -------------------- */

    // 회정 정보 성공 여부 관찰
    private fun observeUserProfile() {
        // 회원정보 수신
        viewLifecycleOwner.lifecycleScope.launch {
            myPageViewModel.userProfile.collect { user ->
                editProfileInfoResponse = user
                bindUserProfile(user)
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

        // 버튼 색상 변경
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(requireContext().getColor(R.color.main_color))

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

/* -------------------- 회원 탈퇴 -------------------- */

    private fun collectSignout() {
        viewLifecycleOwner.lifecycleScope.launch {
            signoutViewModel.signoutSuccess.collect {
                Toast.makeText(requireContext(), "회원 탈퇴에 성공했습니다.", Toast.LENGTH_SHORT).show()

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
