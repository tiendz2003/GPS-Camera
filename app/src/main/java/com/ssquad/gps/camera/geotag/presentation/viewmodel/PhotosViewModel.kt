package com.ssquad.gps.camera.geotag.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssquad.gps.camera.geotag.data.models.Photo
import com.ssquad.gps.camera.geotag.data.models.SortOption
import com.ssquad.gps.camera.geotag.data.models.TemplateDataModel
import com.ssquad.gps.camera.geotag.domain.MediaRepository
import com.ssquad.gps.camera.geotag.utils.Resource
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import com.ssquad.gps.camera.geotag.utils.formatCoordinate
import com.ssquad.gps.camera.geotag.utils.formatToDate
import com.ssquad.gps.camera.geotag.utils.formatToTime
import com.ssquad.gps.camera.geotag.worker.CacheDataTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class PhotosViewModel(
    private val repository: MediaRepository,
    private val cacheDataTemplate: CacheDataTemplate
) : ViewModel() {

    private val _photos = MutableLiveData<Resource<List<Photo>>>()
    val photos: LiveData<Resource<List<Photo>>> = _photos
    private val _selectedPhoto = MutableLiveData<Photo>()
    val selectedPhoto: LiveData<Photo> = _selectedPhoto
    private val _cacheData = MutableStateFlow<TemplateDataModel?>(null)
    val cacheData = _cacheData.asStateFlow()
    fun loadPhotosFromAlbum(albumId: String) {
        viewModelScope.launch {
            _photos.value = Resource.Loading()
            try {
                val result = repository.getPhotosFromAlbum(albumId)
                Log.d("EditAlbumLibraryActivity", "observeViewModel: $result")
                _photos.value = Resource.Success(result)
            } catch (e: Exception) {
                Log.e("EditAlbumLibraryActivity", "Error: ${e.message}")
                _photos.value = Resource.Error("Không tải được: " + e.message.toString())
            }
        }
    }

    private var currentMethod = SortOption.DATE_ADDED
    private var loadedPhotos = listOf<Photo>()
    fun loadPhotosFromAppAlbum() {
        viewModelScope.launch {
            _photos.value = Resource.Loading()
            try {
                val albums = repository.getAlbums()
                val appAlbum = albums.find { it.name == "GPS_CAMERA" }
                if (appAlbum != null) {
                    loadedPhotos = repository.getPhotosFromAlbum(appAlbum.id)
                    sortAndShow(currentMethod)
                } else {
                    _photos.value = Resource.Success(emptyList())
                }
            } catch (e: Exception) {
                Log.e("EditAlbumLibraryActivity", "Error: ${e.message}")
                _photos.value = Resource.Error("Unable to load photos from album")
            }
        }
    }

    fun loadVideosFromAppAlbum() {
        viewModelScope.launch {
            _photos.value = Resource.Loading()
            try {
                val albums = repository.getAlbums()
                val appAlbum = albums.find { it.name == "GPS_CAMERA" }
                if (appAlbum != null) {
                    loadedPhotos = repository.getVideoFromAlbum(appAlbum.id)
                    Log.d("EditAlbumLibraryActivity", "Error: $loadedPhotos")
                    sortAndShow(currentMethod)
                } else {
                    _photos.value = Resource.Success(emptyList())
                }
            } catch (e: Exception) {
                Log.e("EditAlbumLibraryActivity", "Error: $e")
                _photos.value = Resource.Error("Không tải được: " + e.message.toString())
            }
        }
    }

    fun sortPhotos(sortBy: SortOption) {
        currentMethod = sortBy
        sortAndShow(sortBy)
    }

    private fun sortAndShow(sortBy: SortOption) {
        val sortedPhotos = when (sortBy) {
            SortOption.NAME -> loadedPhotos.sortedBy { it.name }
            SortOption.FILE_SIZE -> loadedPhotos.sortedByDescending { it.size }
            SortOption.DATE_ADDED -> loadedPhotos.sortedByDescending { it.dateAdded }
        }

        _photos.value = Resource.Success(sortedPhotos)
    }

    fun getCacheDataTemplate() {
        val now = Date()
        if (cacheDataTemplate.isCacheValid()) {
            cacheDataTemplate.templateData.value?.let {
                val (lat, long, location) = SharePrefManager.getCachedCoordinates()
                    ?: Triple(it.lat?.toDouble(), it.long?.toDouble(), it.location)

                _cacheData.value = TemplateDataModel(
                    location = location,
                    lat = lat.toString(),
                    long = long.toString(),
                    temperatureC = it.temperatureC,
                    temperatureF = it.temperatureF,
                    currentTime = now.formatToTime(),
                    currentDate = now.formatToDate()
                )

                Log.d("PhotosViewModel", "getCacheDataTemplate: $it")
            }
        }
    }


    fun selectedPhoto(photo: Photo) {
        _selectedPhoto.value = photo
    }

    override fun onCleared() {
        super.onCleared()
        cacheDataTemplate.cleanup()
    }
}