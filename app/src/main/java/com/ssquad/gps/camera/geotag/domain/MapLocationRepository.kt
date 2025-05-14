package com.ssquad.gps.camera.geotag.domain

import android.location.Location
import com.ssquad.gps.camera.geotag.utils.LocationResult
import kotlinx.coroutines.flow.Flow

interface MapLocationRepository {
    suspend fun getCurrentLocation(): LocationResult
    suspend fun getAddressFromLocation(location: Location): LocationResult
}