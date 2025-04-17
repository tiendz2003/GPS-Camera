package com.example.baseproject.presentation.settingtab.fragment

import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.baseproject.R
import com.example.baseproject.bases.BaseFragment
import com.example.baseproject.databinding.FragmentCurrentMapBinding
import com.example.baseproject.presentation.hometab.dialog.InfoBottomSheet
import com.example.baseproject.presentation.settingtab.activity.MapSettingActivity
import com.example.baseproject.presentation.settingtab.dialog.CurrentLocationSheet
import com.example.baseproject.presentation.viewmodel.MapSettingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class CurrentMapFragment : BaseFragment<FragmentCurrentMapBinding>(FragmentCurrentMapBinding::inflate),
    OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private val mapSettingViewModel: MapSettingViewModel by activityViewModel()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this@CurrentMapFragment)
        }
    }
    override fun initData() {

    }

    override fun initView() {
        observeViewModel()
    }

    override fun initActionView() {

    }
    private fun updateMapWithLocation(location: Location) {
        googleMap?.let {map->
            map.clear()
            val position = LatLng(location.latitude, location.longitude)
            map.addMarker(
                MarkerOptions().position(position).title("Vị trí hiện tại")
            )
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(position,15f),
                object : GoogleMap.CancelableCallback {
                    override fun onFinish() {
                        mapSettingViewModel.mapSettingState.value.currentLocation?.let {location->
                            //(activity as? MapSettingActivity)?.showBottomSheet(location)
                        }
                    }

                    override fun onCancel() {
                        // Handle cancellation if needed
                    }
                }
            )
        }
    }
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
            uiSettings.isMapToolbarEnabled = true
            setOnMapClickListener {location->
                val newLocation = Location("mapClick").apply {
                    latitude = location.latitude
                    longitude = location.longitude
                }
                mapSettingViewModel.updateLocation(newLocation)
            }
        }
        mapSettingViewModel.mapSettingState.value.currentLocation?.let {location->
            updateMapWithLocation(location)
        }

    }
    fun observeViewModel(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                mapSettingViewModel.mapSettingState.collect { state->
                    state.currentLocation?.let {location->
                        updateMapWithLocation(location)
                    }
                }

            }
        }
    }
    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.mapView.onStop()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        binding.mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        binding.mapView.onDestroy()
        super.onDestroyView()
    }

    override fun onLowMemory() {
        binding.mapView.onLowMemory()
        super.onLowMemory()
    }
}