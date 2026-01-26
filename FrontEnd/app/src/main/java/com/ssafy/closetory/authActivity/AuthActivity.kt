package com.ssafy.closetory.authActivity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.baseCode.base.BaseActivity
import com.ssafy.closetory.databinding.ActivityAuthBinding
import com.ssafy.closetory.homeActivity.HomeActivity

class AuthActivity : BaseActivity<ActivityAuthBinding>(ActivityAuthBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 토큰 있으면 로그인 화면 띄우지 말고 바로 HomeActivity로 보내기
        val access = ApplicationClass.authManager.getAccessToken()

        ApplicationClass.authManager.clearToken()
        if (!access.isNullOrBlank()) {
            startActivity(
                Intent(this, HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
            return
        }
    }
}
