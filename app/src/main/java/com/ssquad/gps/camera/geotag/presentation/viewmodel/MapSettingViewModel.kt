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
        loadInitialLocation()
    }

    private fun updateSettingState(newState: (MapSettingState) -> MapSettingState) {
        return _mapSettingState.update(newState)
    }

    private fun loadInitialLocation() {
        viewModelScope.launch {
            updateSettingState {
                it.copy(
                    isLoading = true,
                    isError = null
                )
            }
            val cachedLocation = getCacheLocation()
            if(cachedLocation != null) {
                updateSettingState {
                    it.copy(
                        currentLocation = cachedLocation.location,
                        isLoading = false,
                        isError = null,
                        currentAddress = cachedLocation.address,
                        yourLocation = cachedLocation.location
                    )
                }
                if(cachedLocation.address.isNullOrEmpty()) {
                    fetchAddressForLocation(cachedLocation.location)
                }
            }else{
                fetchCurrentLocation()
            }

        }
    }
    private fun getCacheLocation():CacheLocationData?{
        SharePrefManager.getCachedCoordinates()?.let { (latitude, longitude, address)  ->
            return CacheLocationData(
                Location("CachedProvider").apply {
                    this.latitude = latitude
                    this.longitude = longitude
                },
                address
            )
        }
        if(cacheDataTemplate.isCacheValid()){
            cacheDataTemplate.templateData.value?.let { cacheData ->
                try {
                    val latitude = cacheData.lat?.toDoubleOrNull()
                    val longitude = cacheData.long?.toDoubleOrNull()
                    val address = cacheData.location
                    if (latitude != null && longitude != null) {
                        return CacheLocationData(
                            Location("CachedProvider").apply {
                                this.latitude = latitude
                                this.longitude = longitude
                            },
                            address
                        )
                    }
                }catch (e:Exception){
                    Log.e("Mapsetting", "lỗi convert: ${e.message}")
                }
            }
        }
        return null
    }
    private fun fetchCurrentLocation(){
        viewModelScope.launch {
            updateSettingState {
                it.copy(
                    isLoading = true,
                    isError = null
                )
            }
            try {
                when (val locationResult = mapLocationRepository.getCurrentLocation()) {
                    is LocationResult.Success -> {
                        val currentLocation = locationResult.location
                        updateSettingState {
                            it.copy(
                                currentLocation = currentLocation,
                                yourLocation = currentLocation,
                                isLoading = false,
                            )
                        }
                        fetchAddressForLocation(currentLocation)
                    }

                    is LocationResult.Error -> {
                        updateSettingState {
                            it.copy(
                                isLoading = false,
                                isError = locationResult.message
                            )
                        }
                    }
                    else ->{
                        updateSettingState {
                            it.copy(
                                isLoading = false,
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
     fun updateSelectedLocation(location: Location) {
        viewModelScope.launch {
            updateSettingState {
                it.copy(
                    currentLocation = location,
                    isLoading = true
                )
            }
            fetchAddressForLocation(location)
        }
    }
    private suspend fun fetchAddressForLocation(location:Location){
        try {
            when (val addressResult = mapLocationRepository.getAddressFromLocation(location)) {
                is LocationResult.Address -> {
                    updateSettingState {
                        it.copy(
                            currentAddress = addressResult.address,
                            isLoading = false
                        )
                    }
                    //saveLocationToCache(location, addressResult.address)
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
     fun saveLocationToCache(location: Location, address: String?) {
        address?.let {
            SharePrefManager.saveCachedCoordinates(location.latitude, location.longitude, it)
        }
    }
    fun yourCurrentLocation(){
        fetchCurrentLocation()
    }
    data class CacheLocationData(
        val location: Location,
        val address: String?
    )
}