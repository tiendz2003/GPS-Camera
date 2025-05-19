package com.ssquad.gps.camera.geotag.presentation.settingtab.fragment

import android.location.Location
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.presentation.mainscreen.bases.BaseFragment
import com.ssquad.gps.camera.geotag.databinding.FragmentChooseLocationBinding
import com.ssquad.gps.camera.geotag.presentation.settingtab.activity.MapSettingState
import com.ssquad.gps.camera.geotag.presentation.viewmodel.MapSettingViewModel
import com.ssquad.gps.camera.geotag.service.MapboxManager
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class ChooseLocationFragment :
    BaseFragment<FragmentChooseLocationBinding>(FragmentChooseLocationBinding::inflate) {
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var onMapClickListener:OnMapClickListener
    private lateinit var mapManager:MapboxManager
    private val viewModel: MapSettingViewModel by activityViewModel()
    override fun initData() {

    }

    override fun initView() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.peekHeight =
            resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)
        initMapBox()
        observeViewModel()
    }

    override fun initActionView() {

    }
    private fun initMapBox(){
        mapManager = MapboxManager(requireContext())

        mapManager.initializeMap(
            mapView = binding.mapView,
            mapStyle = Style.SATELLITE_STREETS,
            onMapReady = {
                initLocationComponent()
                viewModel.mapSettingState.value.currentLocation?.let { location ->
                    mapManager.moveCameraToLocation(location)
                }
            }
        )
        onMapClickListener = mapManager.onMapClickListener { point ->
            updateLocationInfo(point.latitude, point.longitude)

        }
    }
    private fun updateLocationInfo(latitude: Double, longitude: Double) {
        val selectedPosition = Location("MapboxProvider").apply {
            this.latitude = latitude
            this.longitude = longitude
        }
        mapManager.moveCameraToLocation(selectedPosition)
        viewModel.updateSelectedLocation(selectedPosition)
    }



    private fun initLocationComponent() {
        val locationComponentPlugin = binding.mapView.location
        val locationGesturePlugin = binding.mapView.gestures
       /* locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = ImageHolder.from(
                    R.drawable.ic_location
                ),
                shadowImage = ImageHolder.from(
                    R.drawable.ic_location
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)

                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson(),

                )

        }*/
        locationGesturePlugin.addOnMapClickListener(onMapClickListener = onMapClickListener)
        locationComponentPlugin.enabled = true
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mapSettingState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: MapSettingState) {
        binding.progressIndicator.isVisible = state.isLoading
        binding.tvLocationTitle.text = state.currentAddress ?: getString(R.string.unknow_location)
        binding.tvDetailedAddress.text = state.currentAddress ?: getString(R.string.unknow_location)
        state.currentLocation?.let { location ->
            binding.tvLatitude.text = String.format(Locale.getDefault(), "%.5f", location.latitude)
            binding.tvLongitude.text =
                String.format(Locale.getDefault(), "%.5f", location.longitude)

        }

        updateDateTime()
        state.isError?.let { errorMessage ->
            showErrorMessage(errorMessage)
        }
    }

    private fun updateDateTime() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        binding.tvDate.text = dateFormat.format(calendar.time)
        binding.tvTime.text = timeFormat.format(calendar.time)
    }



    private fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapManager.cleanUp()
        binding.mapView.gestures.removeOnMapClickListener(onMapClickListener)
    }
}