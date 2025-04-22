package com.ssquad.gps.camera.geotag.utils

import android.location.Location

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}
sealed class LocationResult{
    data class Success(val location: Location) : LocationResult()
    data class Address(val address: String) : LocationResult()
    data class Error(val message: String) : LocationResult()
}