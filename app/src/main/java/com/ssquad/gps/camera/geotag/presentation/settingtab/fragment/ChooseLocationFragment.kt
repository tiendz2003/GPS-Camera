package com.ssquad.gps.camera.geotag.presentation.settingtab.fragment

import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.bases.BaseFragment
import com.ssquad.gps.camera.geotag.databinding.FragmentChooseLocationBinding
import com.ssquad.gps.camera.geotag.presentation.settingtab.activity.MapSettingState
import com.ssquad.gps.camera.geotag.presentation.viewmodel.MapSettingViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class ChooseLocationFragment :
    BaseFragment<FragmentChooseLocationBinding>(FragmentChooseLocationBinding::inflate) {

    private lateinit var mapboxMap: MapboxMap
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private var currentPointAnnotation: PointAnnotation? = null
    private val onMapClickListener = OnMapClickListener { point ->
        val location = Location("MapboxProvider").apply {
            this.latitude = point.latitude()
            this.longitude = point.longitude()
        }
        updateCameraPosition(location)
        updateLocationInfo(location.latitude, location.longitude)
        updateMarker(point)
        true
    }
    private val viewModel: MapSettingViewModel by activityViewModel()
    override fun initData() {

    }

    override fun initView() {
        binding.mapView.mapboxMap.also { mapboxMap = it }
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.peekHeight =
            resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)
        mapboxMap.loadStyle(Style.MAPBOX_STREETS) {
            val annotationPlugin = binding.mapView.annotations
            pointAnnotationManager = annotationPlugin.createPointAnnotationManager()
            initLocationComponent()
            updateMarker(
                Point.fromLngLat(
                    viewModel.mapSettingState.value.currentLocation?.longitude ?: 0.0,
                    viewModel.mapSettingState.value.currentLocation?.latitude ?: 0.0
                )
            )
        }
        observeViewModel()
    }

    override fun initActionView() {

    }

    private fun updateLocationInfo(latitude: Double, longitude: Double) {
        val selectedPosition = Location("MapboxProvider").apply {
            this.latitude = latitude
            this.longitude = longitude
        }
        viewModel.updateSelectedLocation(selectedPosition)
    }

    private fun updateCameraPosition(location: Location) {
        val cameraPosition = CameraOptions.Builder()
            .center(Point.fromLngLat(location.longitude, location.latitude))
            .zoom(10.0)
            .build()
        mapboxMap.setCamera(cameraPosition)

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
            updateCameraPosition(location)
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

    private fun updateMarker(point: Point) {
        currentPointAnnotation?.let {
            pointAnnotationManager.delete(it)
        }
        val icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_location)
        val bitmap = icon?.let { drawable ->
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } ?: return
        val pointAnnotation = PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(bitmap)
            .withIconAnchor(IconAnchor.BOTTOM)
        currentPointAnnotation = pointAnnotationManager.create(pointAnnotation)
    }

    private fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //binding.mapView.location.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        binding.mapView.gestures.removeOnMapClickListener(onMapClickListener)
    }
}