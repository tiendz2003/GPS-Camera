package com.example.baseproject.presentation.settingtab.activity

import android.location.Location
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityMapSettingBinding
import com.example.baseproject.presentation.settingtab.dialog.CurrentLocationSheet
import com.example.baseproject.presentation.viewmodel.MapSettingViewModel
import com.example.baseproject.service.MapManager
import com.example.baseproject.utils.SharePrefManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapSettingActivity : BaseActivity<ActivityMapSettingBinding>(ActivityMapSettingBinding::inflate) {
    private var currentLocationSheet: CurrentLocationSheet? = null
    private val mapSettingViewModel: MapSettingViewModel by viewModel()
    private lateinit var mapManager: MapManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        mapManager = MapManager(this, lifecycle, binding.mapView)

        setupMapCallbacks()

        binding.mapView.onCreate(savedInstanceState)
    }

    override fun initData() {
    }

    override fun initView() {
        observeViewModel()
    }

    override fun initActionView() {
        with(binding) {
            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setupMapCallbacks() {
        mapManager.setOnMapReadyCallback { map ->
            map.uiSettings.isZoomControlsEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true
            map.uiSettings.isMapToolbarEnabled = true
            map.mapType = SharePrefManager.getMapType()

            mapSettingViewModel.mapSettingState.value.currentLocation?.let { location ->
                mapManager.updateMapWithLocation(location)
                showBottomSheet(location)
            }
        }

        mapManager.setOnMapClickListener { latLng ->
            val newLocation = Location("mapClick").apply {
                latitude = latLng.latitude
                longitude = latLng.longitude
            }
            mapSettingViewModel.updateLocation(newLocation)
        }
    }


    private fun showBottomSheet(location: Location) {
        val currentAddress = mapSettingViewModel.mapSettingState.value.currentAddress ?: "Đang tải địa chỉ..."

        if (currentLocationSheet != null && currentLocationSheet?.isAdded == true) {
            currentLocationSheet?.updateLocationInfo(location, currentAddress)
        } else {
            supportFragmentManager.findFragmentByTag("location_sheet")?.let {
                if (it is DialogFragment) {
                    it.dismiss()
                }
            }
            currentLocationSheet = CurrentLocationSheet(location, currentAddress)
            currentLocationSheet?.show(supportFragmentManager, "location_sheet")
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapSettingViewModel.mapSettingState.collect { state ->
                    state.currentLocation?.let { location ->
                        mapManager.updateMapWithLocation(location)
                        showBottomSheet(location)
                    }

                    state.currentAddress?.let { address ->
                        state.currentLocation?.let { location ->
                            if (currentLocationSheet?.isAdded == true) {
                                currentLocationSheet?.updateLocationInfo(location, address)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapManager.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapManager.onLowMemory()
    }
}