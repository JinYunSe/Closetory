// LoginFragment.kt

package com.ssafy.closetory.authActivity.login

import android.os.Bundle
import android.view.View
import com.ssafy.closetory.R

private const val TAG = "LoginFragment_싸피"

class OnboardingStyleFragment : Fragment(R.layout.fragment_onboarding_style) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvStyleTag)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        // UI 확인용 더미 어댑터
        recyclerView.adapter = DummyTagAdapter()
    }
}
