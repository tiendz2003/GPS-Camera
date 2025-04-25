package com.ssquad.gps.camera.geotag.presentation.settingtab.activity

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.databinding.ActivityMapSettingBinding
import com.ssquad.gps.camera.geotag.presentation.settingtab.dialog.CurrentLocationSheet
import com.ssquad.gps.camera.geotag.presentation.viewmodel.MapSettingViewModel
import com.ssquad.gps.camera.geotag.service.MapManager
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import com.ssquad.gps.camera.geotag.utils.showToast
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapSettingActivity :
    BaseActivity<ActivityMapSettingBinding>(ActivityMapSettingBinding::inflate) {
    private var currentLocationSheet: CurrentLocationSheet? = null
    private val mapSettingViewModel: MapSettingViewModel by viewModel()
    private lateinit var mapManager: MapManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            btnCheck.setOnClickListener {
                val currentCoordinate = mapSettingViewModel.mapSettingState.value.currentLocation
                val currentLocation = mapSettingViewModel.mapSettingState.value.currentAddress
                if (currentCoordinate != null && currentLocation != null) {
                    Log.d(
                        "MapSettingActivity",
                        "Selected location: ${currentCoordinate.latitude}, ${currentCoordinate.longitude}"
                    )
                    SharePrefManager.saveCachedCoordinates(
                        currentCoordinate.latitude,
                        currentCoordinate.longitude,
                        currentLocation
                    )
                    showToast(getString(R.string.location_saved_successfully))
                    finish()
                } else {
                    showToast(getString(R.string.no_location_selected))
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
                val isLoading = mapSettingViewModel.mapSettingState.value.currentAddress == null
                showBottomSheet(location, isLoading)
            }
        }

        mapManager.setOnMapClickListener { latLng ->
            val newLocation = Location("mapClick").apply {
                latitude = latLng.latitude
                longitude = latLng.longitude
            }
            if (currentLocationSheet != null && currentLocationSheet?.isAdded == true) {
                currentLocationSheet?.dismiss()
                currentLocationSheet = null
            }
            // Cập nhật location mới vào view model
            mapSettingViewModel.updateLocation(newLocation)
        }
    }


    private fun showBottomSheet(location: Location, isLoading: Boolean = false) {
        val currentAddress = if (isLoading) {
            getString(R.string.loading_address)
        } else {
            mapSettingViewModel.mapSettingState.value.currentAddress
                ?: getString(R.string.loading_address)
        }

        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is CurrentLocationSheet && fragment !== currentLocationSheet) {
                fragment.dismiss()
            }
        }

        if (currentLocationSheet != null && currentLocationSheet?.isAdded == true) {
            // Cập nhật bottom sheet hiện tại
            currentLocationSheet?.updateLocationInfo(location, currentAddress, isLoading)
        } else {
            // Tạo bottom sheet mới nếu chưa có
            currentLocationSheet =
                CurrentLocationSheet.newInstance(location, currentAddress, isLoading)
            currentLocationSheet?.show(supportFragmentManager, "location_sheet")
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapSettingViewModel.mapSettingState.collect { state ->
                    state.currentLocation?.let { location ->
                        mapManager.updateMapWithLocation(location)

                        val isLoading = state.currentAddress == null

                        showBottomSheet(location, isLoading)
                    }
                }
            }
        }
    }

    fun onBottomSheetDismissed() {
        if (currentLocationSheet != null) {
            currentLocationSheet = null
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

    override fun onDestroy() {
        super.onDestroy()
        currentLocationSheet = null
    }
}