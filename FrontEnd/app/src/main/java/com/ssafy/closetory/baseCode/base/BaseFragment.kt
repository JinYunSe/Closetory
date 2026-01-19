package com.ssafy.ssafyfinalproject.baseCode.base

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

// Fragment의 기본을 작성, 뷰 바인딩 활용
abstract class BaseFragment<B : ViewBinding>(private val bind: (View) -> B, @LayoutRes layoutResId: Int) :
    Fragment(layoutResId) {
    private var _binding: B? = null

    protected val binding get() = _binding!!

    // 하단 bottom nav 존재 여부에 따라 bottom inset 다르게 적용
    open val hasBottomNavigation: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = bind(view)
        applySystemBarInsets(binding.root)
    }

    private fun applySystemBarInsets(rootView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                if (hasBottomNavigation) 0 else systemBars.bottom
            )
            insets
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
}
