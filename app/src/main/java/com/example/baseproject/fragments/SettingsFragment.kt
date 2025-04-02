package com.example.baseproject.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.baseproject.R
import com.example.baseproject.bases.BaseFragment
import com.example.baseproject.databinding.FragmentSettingsBinding

class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {
    override fun initData() {

    }

    override fun initView() {

    }

    override fun initActionView() {
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsFragment()
    }
}