package com.ssafy.closetory.baseCode.base

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding
import com.ssafy.closetory.R

// 액티비티의 기본을 작성, 뷰 바인딩 활용
abstract class BaseActivity<B : ViewBinding>(private val inflate: (LayoutInflater) -> B) : AppCompatActivity() {
    protected lateinit var binding: B
        private set

    // 뷰 바인딩 객체를 받아서 inflate해서 화면을 만들어줌.
    // 즉 매번 onCreate에서 setContentView를 하지 않아도 됨.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = !isNightMode

        applyEdgeToEdgeWithImePadding(binding.root)
    }

    // 토스트를 쉽게 띄울 수 있게 해줌.
    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // 키보드 올라오면 패딩 처리
    protected fun applyEdgeToEdgeWithImePadding(root: View) {
        val initialLeft = root.paddingLeft
        val initialTop = root.paddingTop
        val initialRight = root.paddingRight
        val initialBottom = root.paddingBottom

        val navController = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.bottom_bar_container)
//        val fabAdd = findViewById<View>(R.id.fab_add)
//        val view = findViewById<View>(R.id.bottom_bar_bg)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

            // 키보드가 보이면 Nav바 숨김
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            navController.visibility = if (imeVisible) View.GONE else View.VISIBLE
//            fabAdd.visibility = if (imeVisible) View.GONE else View.VISIBLE
//            view.visibility = if (imeVisible) View.GONE else View.VISIBLE

            v.setPadding(
                initialLeft + sys.left,
                initialTop + sys.top,
                initialRight + sys.right,
                initialBottom + maxOf(sys.bottom, ime.bottom)
            )
            insets
        }
    }
}
