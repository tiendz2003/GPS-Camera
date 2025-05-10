package com.ssquad.gps.camera.geotag.presentation.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssquad.gps.camera.geotag.domain.MapLocationRepository
import com.ssquad.gps.camera.geotag.presentation.settingtab.activity.MapSettingState
import com.ssquad.gps.camera.geotag.utils.LocationResult
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import com.ssquad.gps.camera.geotag.worker.CacheDataTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapSettingViewModel(
    private val mapLocationRepository: MapLocationRepository,
    private val cacheDataTemplate: CacheDataTemplate,
) : ViewModel() {
    private val _mapSettingState = MutableStateFlow(MapSettingState())
    val mapSettingState = _mapSettingState.asStateFlow()

    init {
        getCurrentLocation()
    }

    private fun updateSettingState(newState: (MapSettingState) -> MapSettingState) {
        return _mapSettingState.update(newState)
    }

    private fun getCurrentLocation() {
        viewModelScope.launch {
            if (cacheDataTemplate.isCacheValid()) {
                cacheDataTemplate.templateData.value?.let { cacheData ->
                    try {
                        var latitude: Double? = null
                        var longitude: Double? = null
                        var address: String? = null
                        //ep' kieu
                        if (SharePrefManager.getCachedCoordinates() != null) {
                            latitude = SharePrefManager.getCachedCoordinates()?.first
                            longitude = SharePrefManager.getCachedCoordinates()?.second
                            address = SharePrefManager.getCachedCoordinates()?.third
                        } else {
                            latitude = cacheData.lat?.toDoubleOrNull()
                            longitude = cacheData.long?.toDoubleOrNull()
                            address = cacheData.location
                        }

                        if (latitude != null && longitude != null) {
                            val location = Location("CachedProvider").apply {
                                this.latitude = latitude
                                this.longitude = longitude
                            }

                            updateSettingState {
                                it.copy(
                                    currentLocation = location,
                                    isLoading = false,
                                    isError = null,
                                    currentAddress = address,
                                    yourLocation = location
                                )
                            }
                            updateLocation(location)
                            //
                        }
                    } catch (e: Exception) {
                        Log.e("Mapsetting", "lỗi convert: ${e.message}")
                    }
                }
            }

            updateSettingState {
                it.copy(isLoading = true, isError = null)
            }

            if (cacheDataTemplate.templateData.value == null && SharePrefManager.getCachedCoordinates() == null) {
                try {
                    when (val locationResult = mapLocationRepository.getCurrentLocation()) {
                        is LocationResult.Success -> {
                            val currentLocation = locationResult.location
                            updateSettingState {
                                it.copy(
                                    currentLocation = currentLocation,
                                    yourLocation = currentLocation,
                                    isLoading = false,
                                    isError = null
                                )
                            }
                            updateLocation(currentLocation)
                        }

                        is LocationResult.Error -> {
                            updateSettingState {
                                it.copy(
                                    isLoading = false,
                                    isError = locationResult.message
                                )
                            }
                        }

                        is LocationResult.Address -> {
                            updateSettingState {
                                it.copy(
                                    currentAddress = locationResult.address
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    updateSettingState {
                        it.copy(
                            isLoading = false,
                            isError = e.message
                        )
                    }
                }
            }
        }
    }

    fun updateLocation(location: Location) {
        updateSettingState {
            it.copy(
                currentLocation = location,
                isLoading = true // Set loading while fetching address
            )
        }
        viewModelScope.launch {
            try {
                when (val addressResult = mapLocationRepository.getAddressFromLocation(location)) {
                    is LocationResult.Address -> {
                        updateSettingState {
                            it.copy(
                                currentAddress = addressResult.address,
                                isLoading = false
                            )
                        }
                    }

                    is LocationResult.Error -> {
                        updateSettingState {
                            it.copy(
                                isLoading = false,
                                isError = addressResult.message
                            )
                        }
                    }

                    else -> {
                        updateSettingState { it.copy(isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                updateSettingState {
                    it.copy(
                        isLoading = false,
                        isError = e.message
                    )
                }
            }
        }
    }
}