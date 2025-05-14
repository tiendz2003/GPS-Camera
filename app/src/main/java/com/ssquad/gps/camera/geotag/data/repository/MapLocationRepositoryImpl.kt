package com.ssquad.gps.camera.geotag.data.repository

import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.ssquad.gps.camera.geotag.domain.MapLocationRepository
import com.ssquad.gps.camera.geotag.utils.LocationResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mapbox.geojson.Point
import com.mapbox.search.ApiType
import com.mapbox.search.QueryType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class MapLocationRepositoryImpl(context: Context) : MapLocationRepository {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val searchEngine by lazy {
        SearchEngine.createSearchEngineWithBuiltInDataProviders(
            ApiType.SEARCH_BOX,
            SearchEngineSettings()
        )
    }


    override suspend fun getCurrentLocation(): LocationResult =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                try {
                    val locationRequest = LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        0L
                    )
                        .setMinUpdateIntervalMillis(0L)
                        .setMaxUpdateDelayMillis(0L)
                        .setMaxUpdates(1)
                        .build()
                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                            val location = locationResult.lastLocation
                            fusedLocationClient.removeLocationUpdates(this)
                            if (location != null) {
                                continuation.resume(LocationResult.Success(location))
                            } else {
                                continuation.resume(LocationResult.Error("Không tìm thấy vị trí"))
                            }
                        }
                    }
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                    continuation.invokeOnCancellation {
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                    }
                } catch (e: SecurityException) {
                    continuation.resume(LocationResult.Error("Không có quyền truy cập vị trí:${e.message}"))
                } catch (e: Exception) {
                    continuation.resume(LocationResult.Error("Lỗi lấy vị trí:${e.message}"))
                }
            }
        }

    override suspend fun getAddressFromLocation(location: Location): LocationResult =
        callbackFlow {
            try {
                val searchRequestTask: AsyncOperationTask?
                Log.d(
                    "MapLocationRepository",
                    "Vị trí: ${location.latitude}, ${location.longitude}"
                )
                val point = Point.fromLngLat(
                    location.longitude,
                    location.latitude
                )
                val options = ReverseGeoOptions(
                    center = point,
                    limit = 1,
                    types = listOf(
                        QueryType.ADDRESS,
                        QueryType.PLACE,
                        QueryType.POI,
                        QueryType.LOCALITY
                    ),
                    languages = listOf(IsoLanguageCode.ENGLISH) // Ngôn ngữ tiếng Việt
                )
                searchRequestTask = searchEngine.search(options, object : SearchCallback {
                    override fun onError(e: Exception) {
                        trySend(LocationResult.Error("Lỗi khi tìm địa chỉ: ${e.message}"))
                        close()
                    }

                    override fun onResults(
                        results: List<SearchResult>,
                        responseInfo: ResponseInfo
                    ) {
                        Log.d("MapLocationRepository", "Kết quả tìm kiếm: $results")
                        if (results.isNotEmpty()) {
                            Log.d("MapLocationRepository", "Kết quả tìm kiếm: $results")
                            val result = results[0]
                            val addressText = getFormatAddress(result)
                            trySend(LocationResult.Address(addressText)).isSuccess
                        } else {
                            Log.d("MapLocationRepository", "Không rõ địa chỉ")
                            trySend(LocationResult.Address("Không rõ địa chỉ"))
                        }
                        close()
                    }
                })
                awaitClose {
                    searchRequestTask.cancel()
                }
            } catch (e: Exception) {
                trySend(LocationResult.Error("Lỗi lấy địa chỉ: ${e.message}")).isSuccess
                close(e)
            }
        }
            .retry(3) // Thử lại tối đa 3 lần nếu có lỗi
            .catch { e -> emit(LocationResult.Error("Lỗi không khắc phục được: ${e.message}")) }
            .first()

    private fun getFormatAddress(result: SearchResult): String {
        val address = result.address
        // Lấy thành phố và quốc gia
        val locality = address?.place ?: result.name
        val country = address?.country ?: ""
        return when {
            locality.isNotEmpty() && country.isNotEmpty() -> "$locality, $country"
            locality.isNotEmpty() -> locality
            country.isNotEmpty() -> country
            else -> {
                "Không tìm thấy địa chỉ"
            }
        }
    }
}