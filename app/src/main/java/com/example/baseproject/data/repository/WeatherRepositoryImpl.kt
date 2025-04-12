package com.example.baseproject.data.repository

import android.location.Location
import com.example.baseproject.data.networking.WeatherApiService
import com.example.baseproject.domain.WeatherRepository
import com.example.baseproject.utils.Resource
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
                if (response.isSuccessful && response.body() != null) {
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