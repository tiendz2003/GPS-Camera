package com.ssquad.gps.camera.geotag.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.ssquad.gps.camera.geotag.data.models.TemplateDataModel
import com.ssquad.gps.camera.geotag.domain.MapLocationRepository
import com.ssquad.gps.camera.geotag.domain.WeatherRepository
import com.ssquad.gps.camera.geotag.utils.LocationResult
import com.ssquad.gps.camera.geotag.utils.Resource
import java.util.Locale

class LoadDataTemplateWorker(
    context: Context,
    params:WorkerParameters,
    private val weatherRepository:WeatherRepository,
    private val locationRepository:MapLocationRepository,
    private val cacheDataTemplate: CacheDataTemplate
) :CoroutineWorker(context,params) {
    override suspend fun doWork(): Result {
        try {

            val locationResult = locationRepository.getCurrentLocation()


            if(locationResult is LocationResult.Success){
                val currentLocation = locationResult.location
                val lat = String.format(Locale.getDefault(), "%.6f", currentLocation.latitude)
                val lon = String.format(Locale.getDefault(), "%.6f", currentLocation.longitude)
                val addressResult = locationRepository.getAddressFromLocation(currentLocation)
                val tempResult = weatherRepository.getCurrentTemp(currentLocation)
                val location = if(addressResult is LocationResult.Address){
                    addressResult.address
                }else{
                    null
                }
                val temperature = if(tempResult is Resource.Success){
                    tempResult.data
                }else{
                    val fakeTempResult = weatherRepository.getFakeTemp()
                    if(fakeTempResult is Resource.Success) {
                        fakeTempResult.data
                    }else{
                        null
                    }
                }
                cacheDataTemplate.updateData(
                    TemplateDataModel(
                        location = location?:"Loading...",
                        lat = lat,
                        long = lon,
                        temperature = temperature,
                    )
                )

            }
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    class Factory(
        private val weatherRepository: WeatherRepository,
        private val locationRepository: MapLocationRepository,
        private val cacheDataTemplate: CacheDataTemplate
    ) :WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker? {
            return if (workerClassName == LoadDataTemplateWorker::class.java.name) {
                LoadDataTemplateWorker(
                    appContext,
                    workerParameters,
                    weatherRepository,
                    locationRepository,
                    cacheDataTemplate
                )
            }else{
                null
            }
        }

    }

}