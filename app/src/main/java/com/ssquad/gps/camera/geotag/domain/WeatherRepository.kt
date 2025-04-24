package com.ssquad.gps.camera.geotag.domain

import android.location.Location
import com.ssquad.gps.camera.geotag.utils.Resource

interface WeatherRepository {
    suspend fun getCurrentTemp(location: Location):  Resource<Pair<Float?, Float?>>
    suspend fun getFakeTemp(): Resource<Pair<Float?, Float?>>
}