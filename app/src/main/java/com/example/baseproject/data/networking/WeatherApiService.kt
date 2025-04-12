package com.example.baseproject.data.networking

import com.example.baseproject.data.models.WttrResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherApiService {
    @GET("{lat},{lon}?format=j1")
    suspend fun getCurrentTemp(
        @Path("lat") lat: Double,
        @Path("lon") lon: Double
    ): Response<WttrResponse>
}