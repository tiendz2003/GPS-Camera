package com.example.baseproject.service

import android.content.Context
import android.location.Location
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.example.baseproject.R
import com.example.baseproject.utils.SharePrefManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapManager(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val mapView: MapView
) : DefaultLifecycleObserver {

    private var googleMap: GoogleMap? = null
    private var onMapReadyCallback: ((GoogleMap) -> Unit)? = null
    private var mapClickListener: ((LatLng) -> Unit)? = null
    private var animationFinishListener: (() -> Unit)? = null

    init {
        lifecycle.addObserver(this)
        mapView.getMapAsync { map ->
            googleMap = map
            setupMap()
            onMapReadyCallback?.invoke(map)
        }
    }

    private fun setupMap() {
        googleMap?.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
            uiSettings.isMapToolbarEnabled = true
            mapType = SharePrefManager.getMapType()
            setOnMapClickListener { location ->
                mapClickListener?.invoke(location)
            }
        }
    }

    fun setOnMapReadyCallback(callback: (GoogleMap) -> Unit) {
        googleMap?.let { callback(it) } ?: run {
            onMapReadyCallback = callback
        }
    }

    fun setOnMapClickListener(listener: (LatLng) -> Unit) {
        mapClickListener = listener
        googleMap?.setOnMapClickListener { location ->
            listener(location)
        }
    }

    fun setOnAnimationFinishListener(listener: () -> Unit) {
        animationFinishListener = listener
    }

    fun updateMapWithLocation(location: Location, animate: Boolean = true) {
        googleMap?.let { map ->
            map.clear()
            val position = LatLng(location.latitude, location.longitude)
            map.addMarker(
                MarkerOptions().position(position).title(context.getString(R.string.current_location))
            )

            if (animate) {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(position, 15f),
                    object : GoogleMap.CancelableCallback {
                        override fun onFinish() {
                            animationFinishListener?.invoke()
                        }

                        override fun onCancel() {
                            // Handle cancellation if needed
                        }
                    }
                )
            } else {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(position, 15f)
                )
                animationFinishListener?.invoke()
            }
        }
    }

    fun updateMapType(mapType: Int) {
        googleMap?.mapType = mapType
    }

    // DefaultLifecycleObserver methods
    override fun onStart(owner: LifecycleOwner) {
        mapView.onStart()
    }

    override fun onResume(owner: LifecycleOwner) {
        mapView.onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        mapView.onPause()
    }

    override fun onStop(owner: LifecycleOwner) {
        mapView.onStop()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        mapView.onDestroy()
    }

    fun onSaveInstanceState(outState: Bundle) {
        mapView.onSaveInstanceState(outState)
    }

    fun onLowMemory() {
        mapView.onLowMemory()
    }
}