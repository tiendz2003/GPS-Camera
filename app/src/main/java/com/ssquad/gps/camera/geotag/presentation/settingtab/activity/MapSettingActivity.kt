package com.ssquad.gps.camera.geotag.presentation.settingtab.activity

import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.DialogFragment
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
                val currentLocation = mapSettingViewModel.mapSettingState.value.currentLocation
                currentLocation?.let { location ->
                    SharePrefManager.saveCachedCoordinates(location.latitude, location.longitude)
                    showToast(getString(R.string.location_saved_successfully))
                    finish()
                } ?: run {
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
                // Kiểm tra trạng thái loading trước khi hiển thị bottom sheet
                val isLoading = mapSettingViewModel.mapSettingState.value.currentAddress == null
                showBottomSheet(location, isLoading)
            }
        }

        mapManager.setOnMapClickListener { latLng ->
            val newLocation = Location("mapClick").apply {
                latitude = latLng.latitude
                longitude = latLng.longitude
            }
            // Đóng bottom sheet cũ nếu có
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
            mapSettingViewModel.mapSettingState.value.currentAddress ?: getString(R.string.loading_address)
        }

        // Đóng tất cả các bottom sheet cũ
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
            currentLocationSheet = CurrentLocationSheet.newInstance(location, currentAddress, isLoading)
            currentLocationSheet?.show(supportFragmentManager, "location_sheet")
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapSettingViewModel.mapSettingState.collect { state ->
                    state.currentLocation?.let { location ->
                        // Cập nhật map
                        mapManager.updateMapWithLocation(location)

                        // Xác định trạng thái loading
                        val isLoading = state.currentAddress == null

                        // Hiển thị hoặc cập nhật bottom sheet với trạng thái loading
                        showBottomSheet(location, isLoading)
                    }
                }
            }
        }
    }

    fun onBottomSheetDismissed() {
        // Đặt lại tham chiếu khi sheet bị đóng
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