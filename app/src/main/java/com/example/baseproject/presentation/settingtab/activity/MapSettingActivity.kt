package com.example.baseproject.presentation.settingtab.activity

import android.location.Location
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityMapSettingBinding
import com.example.baseproject.presentation.settingtab.dialog.CurrentLocationSheet
import com.example.baseproject.presentation.settingtab.fragment.CurrentMapFragment
import com.example.baseproject.presentation.settingtab.fragment.ManualMapFragment
import com.example.baseproject.presentation.viewmodel.MapSettingViewModel
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapSettingActivity : BaseActivity<ActivityMapSettingBinding>(ActivityMapSettingBinding::inflate) {
    private var currentLocationSheet: CurrentLocationSheet ? = null
    private val mapSettingViewModel: MapSettingViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

    }

    override fun initData() {

    }

    override fun initView() {
        setupViewPager()

    }

    override fun initActionView() {
        with(binding){
            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }
    fun showBottomSheet(location: Location) {
        //update lai UI neu sheet da duoc tao
        if (currentLocationSheet != null && currentLocationSheet?.isAdded == true) {
            currentLocationSheet?.updateLocationInfo(location, "Hà Nội,Hoài Đức")
        } else {
            supportFragmentManager.findFragmentByTag("location_sheet")?.let {
                if (it is DialogFragment) {
                    it.dismiss()
                }
            }
            //luon tao 1 sheet
            currentLocationSheet = CurrentLocationSheet(location,  "Hà Nội,Hoài Đức")
            currentLocationSheet?.show(supportFragmentManager, "location_sheet")
        }
    }

    private fun setupViewPager() {
        val pagerAdapter = PreviewPagerAdapter(this)
        binding.viewPager.apply {
            adapter = pagerAdapter
            isUserInputEnabled = false
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Current"
                else -> "Manual"
            }
        }.attach()
    }
    inner class PreviewPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> CurrentMapFragment()
                else -> ManualMapFragment()
            }
        }

        override fun getItemCount(): Int {
            return 2
        }

    }
}