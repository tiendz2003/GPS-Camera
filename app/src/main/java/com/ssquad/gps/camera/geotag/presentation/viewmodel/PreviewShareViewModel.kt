package com.ssquad.gps.camera.geotag.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssquad.gps.camera.geotag.data.models.TemplateState
import com.ssquad.gps.camera.geotag.domain.CameraRepository
import com.ssquad.gps.camera.geotag.presentation.mainscreen.activity.PreviewState
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.data.models.TemplateDataModel
import com.ssquad.gps.camera.geotag.domain.MapLocationRepository
import com.ssquad.gps.camera.geotag.domain.WeatherRepository
import com.ssquad.gps.camera.geotag.utils.LocationResult
import com.ssquad.gps.camera.geotag.utils.Resource
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import com.ssquad.gps.camera.geotag.utils.formatToDate
import com.ssquad.gps.camera.geotag.utils.formatToTime
import com.ssquad.gps.camera.geotag.worker.CacheDataTemplate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class PreviewShareViewModel(
    private val cameraRepository: CameraRepository,
    private val cacheDataTemplate: CacheDataTemplate,
    private val weatherRepository: WeatherRepository,
    private val locationRepository: MapLocationRepository,
    ) : ViewModel() {
    private val _previewUiState = MutableStateFlow(PreviewState())
    val previewUiState = _previewUiState.asStateFlow()

    private val _toastEvents = Channel<String>(Channel.BUFFERED)
    val toastEvents = _toastEvents.receiveAsFlow()

    private val _navigationEvents = Channel<Unit>(Channel.BUFFERED)
    val navigationEvents = _navigationEvents.receiveAsFlow()

    fun updatePreviewState(update: (PreviewState) -> PreviewState) {
        return _previewUiState.update(update)
    }
    fun updateTemplateState(newState: TemplateState){
        updatePreviewState {
            it.copy(
                templateState = newState
            )
        }
    }
    fun setSelectedTemplate(templateId: String) {
        updatePreviewState {
            it.copy(
                selectedTemplateId = templateId
            )
        }
    }
    fun resetCustomTemplate() {
        updatePreviewState {
            it.copy(
                templateState = TemplateState()
            )
        }
    }

    fun saveImageToGallery(context: Context, bitmap: Bitmap,address: String?) {
        viewModelScope.launch {
            updatePreviewState {
                it.copy(
                    isSaving = true
                )
            }
            _toastEvents.send(context.getString(R.string.saving_image))
            try {
                val uri = cameraRepository.saveImageToGallery(context, bitmap,address)
                updatePreviewState {
                    it.copy(
                        savedImageUri = uri,
                    )
                }
                _toastEvents.send(context.getString(R.string.image_saved_successfully))
                _navigationEvents.send(Unit)
            } catch (e: Exception) {
                updatePreviewState {
                    it.copy(
                        error = e.message,
                    )
                }
                _toastEvents.send(context.getString(R.string.save_image_failed))
                _navigationEvents.send(Unit)
            }finally {
                updatePreviewState {
                    it.copy(
                        isSaving = false
                    )
                }
            }
        }
    }
    fun getCacheDataTemplate() {
        val now = Date()
        val customDate = SharePrefManager.getString("CUSTOM_DATE", "")?:now.formatToDate()
        val customTime = SharePrefManager.getString("CUSTOM_TIME", "")?:now.formatToTime()
        val selectedOption = SharePrefManager.getString("DATE_TIME_OPTION", "current")
        Log.d("PhotosViewModel", "CUSTOM_DATE: $customDate")
        Log.d("PhotosViewModel", "CUSTOM_TIME: $customTime")
        Log.d("PhotosViewModel", "DATE_TIME_OPTION: $selectedOption")
        val currentDate = if (selectedOption == "custom" && customDate.isNotEmpty() && customTime.isNotEmpty()) {
            customDate
        } else {
            now.formatToDate()
        }

        val currentTime = if (selectedOption == "custom" && customDate.isNotEmpty() && customTime.isNotEmpty()) {
            customTime
        } else {
            now.formatToTime()
        }
        Log.d("PhotosViewModel", "CUrrentDATE: $currentDate")
        Log.d("PhotosViewModel", "CUrrent_TIME: $currentTime")
        if (cacheDataTemplate.isCacheValid()) {
            cacheDataTemplate.templateData.value?.let {data->
                val (lat, long, location) = SharePrefManager.getCachedCoordinates()
                    ?: Triple(data.lat?.replace(",", ".")?.toDouble(), data.long?.replace(",", ".")?.toDouble(), data.location)

                updatePreviewState {
                    it.copy(
                        cacheDataTemplate = TemplateDataModel(
                            location = location,
                            lat = lat.toString(),
                            long = long.toString(),
                            temperatureC = data.temperatureC,
                            temperatureF = data.temperatureF,
                            currentTime = currentTime,
                            currentDate = currentDate
                        )
                    )
                }

            }
        }
        if (!cacheDataTemplate.isCacheValid() || cacheDataTemplate.templateData.value == null) {
            viewModelScope.launch {
                try {
                    val locationResult = locationRepository.getCurrentLocation()
                    if (locationResult is LocationResult.Success) {
                        val currentLocation = locationResult.location
                        val lat = String.format(Locale.getDefault(), "%.6f", currentLocation.latitude)
                        val lon = String.format(Locale.getDefault(), "%.6f", currentLocation.longitude)
                        val addressResult = locationRepository.getAddressFromLocation(currentLocation)
                        val tempResult = weatherRepository.getCurrentTemp(currentLocation)
                        val fakeTempResult = weatherRepository.getFakeTemp()

                        val location = if (addressResult is LocationResult.Address) {
                            addressResult.address
                        } else {
                            null
                        }

                        val tempPair = (if (tempResult is Resource.Success) tempResult.data else null)
                            ?: (if (fakeTempResult is Resource.Success) fakeTempResult.data else null)
                            ?: Pair(null, null)

                        val tempC = tempPair.first
                        val tempF = tempPair.second

                        updatePreviewState {
                            it.copy(
                                cacheDataTemplate =TemplateDataModel(
                                    location = location,
                                    lat = lat,
                                    long = lon,
                                    temperatureC = tempC,
                                    temperatureF = tempF,
                                    currentTime = currentTime,
                                    currentDate = currentDate
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PhotosViewModel", "Error: ${e.message}")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cacheDataTemplate.cleanup()
    }
}