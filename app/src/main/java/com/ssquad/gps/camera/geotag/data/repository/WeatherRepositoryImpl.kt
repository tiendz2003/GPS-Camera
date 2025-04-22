package com.ssquad.gps.camera.geotag.data.repository

import android.location.Location
import com.ssquad.gps.camera.geotag.data.networking.WeatherApiService
import com.ssquad.gps.camera.geotag.domain.WeatherRepository
import com.ssquad.gps.camera.geotag.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Random

class WeatherRepositoryImpl(
    private val weatherApiService: WeatherApiService
): WeatherRepository {
    override suspend fun getCurrentTemp(location: Location): Resource<String> = withContext(
        Dispatchers.IO){
            try {
                val response = weatherApiService.getCurrentTemp(
                    lat = location.latitude,
                    lon = location.longitude,
                )
                if (response.isSuccessful) {
                    val currentCondition = response.body()?.currentCondition?.firstOrNull()
                    if(currentCondition != null){
                        val temp = "${currentCondition.tempC}°C"
                        Resource.Success(temp)
                    }else{
                        Resource.Error("Không tìm thấy dữ liệu thời tiết")
                    }
                } else {
                    Resource.Error("Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Exception: ${e.message}")
            }
    }

    override suspend fun getFakeTemp(): Resource<String> = withContext(Dispatchers.IO){
        try {
            val random = Random()
            val fakeTemp = 10 + random.nextInt(20)
            Resource.Success("$fakeTemp °C")
        }catch (e: Exception){
            Resource.Error("Exception: ${e.message}")
        }
    }

}