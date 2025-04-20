package com.example.baseproject.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.baseproject.R
import com.example.baseproject.bases.BaseFragment
import com.example.baseproject.databinding.FragmentSettingsBinding
import com.example.baseproject.presentation.hometab.activities.LanguageActivity
import com.example.baseproject.presentation.settingtab.activity.CustomDateTimeActivity
import com.example.baseproject.presentation.settingtab.activity.DateTimeFormatActivity
import com.example.baseproject.presentation.settingtab.activity.MapSettingActivity
import com.example.baseproject.presentation.settingtab.activity.MapTypeActivity
import com.example.baseproject.presentation.settingtab.activity.SetupTempActivity
import com.example.baseproject.utils.Constants

private const val URL = "param1"
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {
    override fun initData() {

    }

    override fun initView() {

    }

    override fun initActionView() {
        with(binding){
            llMapSetting.setOnClickListener {
                startActivity(Intent(requireContext(), MapSettingActivity::class.java))
            }
            llMapType.setOnClickListener {
                startActivity(Intent(requireContext(), MapTypeActivity::class.java))
            }
            llDatetimeType.setOnClickListener {
                startActivity(Intent(requireContext(), CustomDateTimeActivity::class.java))
            }
            llDatetimeFormat.setOnClickListener {
                startActivity(Intent(requireContext(), DateTimeFormatActivity::class.java))
            }
            llTempUnit.setOnClickListener {
                startActivity(Intent(requireContext(), SetupTempActivity::class.java))
            }
            llLgSetting.setOnClickListener {
                val intent = Intent(requireContext(), LanguageActivity::class.java).apply {
                    putExtra(Constants.LANGUAGE_EXTRA, true)
                }
                startActivity(intent)
            }
            llShare.setOnClickListener {
                // Handle share action
            }
            llRating.setOnClickListener {
                // Handle rate action
            }
        }
    }
}