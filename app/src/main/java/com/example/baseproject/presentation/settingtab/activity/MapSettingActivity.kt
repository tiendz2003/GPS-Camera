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
import com.example.baseproject.utils.SharePrefManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapSettingActivity : BaseActivity<ActivityMapSettingBinding>(ActivityMapSettingBinding::inflate),OnMapReadyCallback  {
    private var currentLocationSheet: CurrentLocationSheet ? = null
    private val mapSettingViewModel: MapSettingViewModel by viewModel()
    private var googleMap: GoogleMap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        with(binding){
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this@MapSettingActivity)

        }
    }

    override fun initData() {

    }

    override fun initView() {
        observeViewModel()
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
                            showBottomSheet(location)
                        }
                    }

                    override fun onCancel() {

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
            mapType = SharePrefManager.getMapType()
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


    override fun onLowMemory() {
        binding.mapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onDestroy() {
        binding.mapView.onDestroy()
        super.onDestroy()
    }
}