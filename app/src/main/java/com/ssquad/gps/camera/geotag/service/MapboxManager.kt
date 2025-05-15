package com.ssquad.gps.camera.geotag.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.gms.maps.model.LatLng
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapSnapshotOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Size
import com.mapbox.maps.Snapshotter
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteOptions
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import com.ssquad.gps.camera.geotag.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MapboxManager(private val context: Context) {
    private lateinit var mapboxMap: MapboxMap
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private var currentPointAnnotation: PointAnnotation? = null
    private val placeAutoComplete by lazy {
        PlaceAutocomplete.create()
    }
    private var snapshotter: Snapshotter? = null


    fun onMapClickListener(onMapClick: (LatLng) -> Unit): OnMapClickListener {
        return OnMapClickListener { point ->
            val location = Location("MapboxProvider").apply {
                this.latitude = point.latitude()
                this.longitude = point.longitude()
            }
            moveCameraToLocation(location)
            updateMarker(
                Point.fromLngLat(
                    location.longitude, location.latitude
                )
            )
            onMapClick(LatLng(location.latitude, location.longitude))
            true
        }
    }

    fun initializeMap(mapView: MapView, mapStyle: String, onMapReady: (MapboxMap) -> Unit = {}) {
        mapboxMap = mapView.mapboxMap
        mapboxMap.loadStyle(mapStyle) {
            val annotationPlugin = mapView.annotations
            pointAnnotationManager = annotationPlugin.createPointAnnotationManager()
            onMapReady(mapboxMap)
        }
    }

    fun moveCameraToLocation(location: Location, zoom: Double = 10.0) {
        val cameraPosition = CameraOptions.Builder().center(
            Point.fromLngLat(location.longitude, location.latitude)
        )
            .zoom(zoom)
            .build()
        mapboxMap.setCamera(cameraPosition)
        updateMarker(
            Point.fromLngLat(
                location.longitude, location.latitude
            )
        )
    }

    private fun updateMarker(point: Point) {
        currentPointAnnotation?.let {
            pointAnnotationManager.delete(it)
        }
        val icon = AppCompatResources.getDrawable(context, R.drawable.ic_location)
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
        val pointAnnotation =
            PointAnnotationOptions().withPoint(point).withIconImage(bitmap).withIconAnchor(
                IconAnchor.BOTTOM
            )
        currentPointAnnotation = pointAnnotationManager.create(pointAnnotation)
    }

    suspend fun searchPlaces(
        query: String,
        proximityLocation: Location?,
        onSearchStarted: () -> Unit = {},
        onSearchResult: (List<PlaceAutocompleteSuggestion>) -> Unit
    ) {
        onSearchStarted()
        val response = placeAutoComplete.suggestions(
            query = query,
            options = PlaceAutocompleteOptions(limit = 10),
            proximity = proximityLocation?.let { location ->
                Point.fromLngLat(location.longitude, location.latitude)
            } ?: Point.fromLngLat(0.0, 0.0)
        )
        if (response.isValue) {
            val suggestions = requireNotNull(response.value)
            onSearchResult(suggestions)
        }
    }

    suspend fun selectPlace(
        suggestion: PlaceAutocompleteSuggestion,
        onSuccess: (Location) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val selectionResponse = placeAutoComplete.select(suggestion)
        selectionResponse.onValue { result ->
            val coordinate = result.coordinate
            val location = Location("SearchResult").apply {
                latitude = coordinate.latitude()
                longitude = coordinate.longitude()
            }
            onSuccess(location)
        }.onError { e ->
            Log.e("MapManager", "Lỗi khi chọn địa điểm: ${e.message}")
            onError(e)
        }
    }


    fun createSnapshot(
        latitude: Double,
        longitude: Double,
        zoom: Double = 14.0,
        mapStyle: String = Style.STANDARD,
        onSnapshotReady: (Bitmap) -> Unit,
        onError: (String) -> Unit
    ) {
        val snapshotterOptions = MapSnapshotOptions.Builder()
            .size(Size(512f, 512f))
            .pixelRatio(1.0f)
            .build()

        val snapshotter = Snapshotter(context, snapshotterOptions).apply {
            setStyleUri(mapStyle)
            setCamera(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(longitude, latitude))
                    .zoom(zoom)
                    .build()
            )

            start(
                overlayCallback = { snapshot ->
                    try {
                        val drawable = AppCompatResources.getDrawable(context, R.drawable.ic_location)
                        val markerBitmap = drawable?.let {
                            val width = it.intrinsicWidth
                            val height = it.intrinsicHeight
                            it.setBounds(0, 0, width, height)
                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(bitmap)
                            it.draw(canvas)
                            bitmap
                        }


                        // Chuyển đổi tọa độ địa lý sang tọa độ màn hình
                        val screenCoordinate = snapshot.screenCoordinate(
                            Point.fromLngLat(longitude, latitude)
                        )

                        val x = screenCoordinate.x.toFloat() - (markerBitmap!!.width / 2f)
                        val y = screenCoordinate.y.toFloat() - markerBitmap.height.toFloat()

                        // Vẽ marker với tất cả tọa độ là Float
                        snapshot.canvas.drawBitmap(markerBitmap, x, y, null)
                    } catch (e: Exception) {
                        Log.e("Snapshotter", "Lỗi khi vẽ marker: ${e.message}", e)
                    }
                }
            ) { bitmap, error ->
                if (error != null) {
                    onError("Lỗi tạo ảnh chụp: $error")
                } else if (bitmap != null) {
                    onSnapshotReady(bitmap)
                } else {
                    onError("Không thể tạo ảnh chụp")
                }
            }
        }

        this.snapshotter = snapshotter
    }

    fun getMapboxMap() = mapboxMap
     fun destroySnapshotter() {
        snapshotter?.destroy()
        snapshotter = null
    }
    fun cleanUp() {
        pointAnnotationManager.deleteAll()
        currentPointAnnotation = null
        destroySnapshotter()
    }
}