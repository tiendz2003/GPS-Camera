package com.ssquad.gps.camera.geotag.presentation.settingtab.activity

import android.location.Location
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.databinding.ActivityMapSettingBinding
import com.ssquad.gps.camera.geotag.presentation.settingtab.dialog.CurrentLocationSheet
import com.ssquad.gps.camera.geotag.presentation.viewmodel.MapSettingViewModel
import com.ssquad.gps.camera.geotag.service.MapManager
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
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
            btnMyLocation.setOnClickListener {
                mapSettingViewModel.mapSettingState.value.yourLocation?.let { location ->
                    mapManager.updateMapWithLocation(location)
                    showBottomSheet(location)
                }
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