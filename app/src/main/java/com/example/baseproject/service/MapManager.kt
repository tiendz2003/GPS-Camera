package com.example.baseproject.service

import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.baseproject.R
import com.example.baseproject.utils.SharePrefManager
import com.example.baseproject.utils.loadImageIcon
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.core.graphics.scale

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
                MarkerOptions().position(position)
                    .title(context.getString(R.string.current_location))
            )

            if (animate) {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(position, 15f),
                    object : GoogleMap.CancelableCallback {
                        override fun onFinish() {
                            animationFinishListener?.invoke()
                        }

                        override fun onCancel() {

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

    fun captureMapSnapshot(googleMap: GoogleMap, callback: (Bitmap) -> Unit) {
        Handler(Looper.getMainLooper()).post {
            val snapshotReadyCallback = GoogleMap.SnapshotReadyCallback { bitmap ->
                bitmap?.let {
                    callback(it)
                }
            }
            googleMap.snapshot(snapshotReadyCallback)
        }
    }
    fun captureMapImage(lat: Double, lon: Double, zoom: Float = 15f, callback: (Bitmap?) -> Unit) {
        // Kiểm tra trạng thái lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            callback(null)
            return
        }

        googleMap?.let { map ->
            map.clear()
            val position = LatLng(lat, lon)
            map.addMarker(
                MarkerOptions().position(position)
                    .title(context.getString(R.string.current_location))
            )
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom))

            Handler(Looper.getMainLooper()).postDelayed({
                // Kiểm tra lại trạng thái lifecycle trước khi chụp
                if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    callback(null)
                    return@postDelayed
                }

                captureMapSnapshot(map) { bitmap ->
                    callback(bitmap)
                }
            }, 300)
        } ?: run {
            callback(null)
        }
    }
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