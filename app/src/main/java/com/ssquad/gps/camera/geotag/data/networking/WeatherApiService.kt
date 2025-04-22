package com.ssquad.gps.camera.geotag.data.networking

import com.ssquad.gps.camera.geotag.data.models.WttrResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface WeatherApiService {
    @GET("{lat},{lon}?format=j1")
    suspend fun getCurrentTemp(
        @Path("lat") lat: Double,
        @Path("lon") lon: Double
    ): Response<WttrResponse>
}