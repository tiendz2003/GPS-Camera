package com.ssquad.gps.camera.geotag.data.repository

import android.location.Location
import android.util.Log
import com.ssquad.gps.camera.geotag.data.networking.WeatherApiService
import com.ssquad.gps.camera.geotag.domain.WeatherRepository
import com.ssquad.gps.camera.geotag.utils.Resource
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Random

class WeatherRepositoryImpl(
    private val weatherApiService: WeatherApiService
): WeatherRepository {
    override suspend fun getCurrentTemp(location: Location): Resource<Pair<Float?, Float?>> = withContext(
        Dispatchers.IO){
            try {
                val response = weatherApiService.getCurrentTemp(
                    lat = location.latitude,
                    lon = location.longitude,
                )
                if (response.isSuccessful) {
                    val currentCondition = response.body()?.currentCondition?.firstOrNull()
                    if(currentCondition != null){
                        val tempC = currentCondition.tempC.toFloatOrNull()
                        val tempF = currentCondition.tempF.toFloatOrNull()
                        return@withContext Resource.Success(Pair(tempC, tempF))
                    }else{
                        Resource.Error("No weather data found")
                    }
                } else {
                    Resource.Error("Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Exception: ${e.message}")
            }
    }

    override suspend fun getFakeTemp(): Resource<Pair<Float?, Float?>> = withContext(Dispatchers.IO){
        try {
            val random = Random()
            val fakeTemp = 10 + random.nextInt(20)
            Resource.Success(Pair(fakeTemp.toFloat(), fakeTemp.toFloat()))
        }catch (e: Exception){
            Resource.Error("Exception: ${e.message}")
        }
    }

}