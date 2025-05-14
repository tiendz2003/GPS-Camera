package com.ssquad.gps.camera.geotag.presentation.settingtab.activity

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.databinding.ActivityMapSettingBinding
import com.ssquad.gps.camera.geotag.presentation.settingtab.dialog.CurrentLocationSheet
import com.ssquad.gps.camera.geotag.presentation.settingtab.fragment.ChooseLocationFragment
import com.ssquad.gps.camera.geotag.presentation.settingtab.fragment.SearchLocationFragment
import com.ssquad.gps.camera.geotag.presentation.viewmodel.MapSettingViewModel
import com.ssquad.gps.camera.geotag.service.MapManager
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import com.ssquad.gps.camera.geotag.utils.showToast
import com.ssquad.gps.camera.geotag.utils.visible
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapSettingActivity :
    BaseActivity<ActivityMapSettingBinding>(ActivityMapSettingBinding::inflate) {


    override fun initData() {

    }

    override fun initView() {
        setupViewPager()
    }

    override fun initActionView() {

    }


    private fun setupViewPager() {
        val pagerAdapter = PreviewPagerAdapter(this)
        binding.viewPager.apply {
            adapter = pagerAdapter
            isUserInputEnabled = false
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.current)
                else -> getString(R.string.manual)
            }
        }.attach()
    }

    inner class PreviewPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ChooseLocationFragment()
                else -> SearchLocationFragment()
            }
        }

        override fun getItemCount(): Int {
            return 2
        }

    }
}