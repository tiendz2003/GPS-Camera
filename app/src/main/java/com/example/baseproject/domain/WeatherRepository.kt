package com.example.baseproject.domain

import android.location.Location
import com.example.baseproject.utils.Resource

interface WeatherRepository {
    suspend fun getCurrentTemp(location: Location): Resource<String>
    suspend fun getFakeTemp(): Resource<String>
}