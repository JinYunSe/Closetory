package com.ssafy.common_project.homeActivity.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ssafy.common_project.R
import com.ssafy.common_project.databinding.FragmentDashboardBinding
import com.ssafy.common_project.databinding.FragmentHomeBinding
import com.ssafy.ssafyfinalproject.baseCode.base.BaseFragment

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::bind, R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}