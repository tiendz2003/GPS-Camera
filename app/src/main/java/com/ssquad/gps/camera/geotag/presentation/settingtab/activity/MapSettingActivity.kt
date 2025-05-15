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
import com.google.android.material.snackbar.Snackbar
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
import com.ssquad.gps.camera.geotag.utils.setOnDebounceClickListener
import com.ssquad.gps.camera.geotag.utils.showToast
import com.ssquad.gps.camera.geotag.utils.visible
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapSettingActivity :
    BaseActivity<ActivityMapSettingBinding>(ActivityMapSettingBinding::inflate) {
    private val viewmodel:MapSettingViewModel by viewModel()

    override fun initData() {

    }

    override fun initView() {
        setupViewPager()
        observeViewModel()
    }

    override fun initActionView() {
    }

    private fun observeViewModel(){
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.mapSettingState.collect { state ->
                    binding.btnCheck.isEnabled = !state.isLoading
                    if (state.isError != null) {
                        showToast(state.isError)
                    }
                    if (state.currentLocation != null && state.currentAddress != null) {
                        binding.btnCheck.setOnDebounceClickListener {
                            Log.d("MapSettingActivity", "onClick: ${state.currentLocation}")
                            viewmodel.saveLocationToCache(state.currentLocation, state.currentAddress)
                            Snackbar.make(
                                binding.root,
                                getString(R.string.location_saved_successfully),
                                Snackbar.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }
                }
            }
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