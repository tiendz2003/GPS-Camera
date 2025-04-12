package com.example.baseproject.domain

import android.location.Location
import com.example.baseproject.utils.LocationResult

interface MapLocationRepository {
    suspend fun getCurrentLocation(): LocationResult
    suspend fun getAddressFromLocation(location: Location): LocationResult
}