package com.example.baseproject.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject.data.models.Photo
import com.example.baseproject.data.models.SortOption
import com.example.baseproject.domain.MapLocationRepository
import com.example.baseproject.domain.MediaRepository
import com.example.baseproject.domain.WeatherRepository
import com.example.baseproject.utils.LocationResult
import com.example.baseproject.utils.Resource
import kotlinx.coroutines.launch

class PhotosViewModel(
    private val repository: MediaRepository,
) : ViewModel() {

    private val _photos = MutableLiveData<Resource<List<Photo>>>()
    val photos: LiveData<Resource<List<Photo>>> = _photos
    private val _selectedPhoto = MutableLiveData<Photo>()
    val selectedPhoto: LiveData<Photo> = _selectedPhoto
    fun loadPhotosFromAlbum(albumId: String) {
        viewModelScope.launch {
            _photos.value = Resource.Loading()
            try {
                val result = repository.getPhotosFromAlbum(albumId)
                Log.d("EditAlbumLibraryActivity", "observeViewModel: $result")
                _photos.value = Resource.Success(result)
            } catch (e: Exception) {
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
                Log.e("EditAlbumLibraryActivity", "Error: $e")
                _photos.value = Resource.Error("Không tải được: " + e.message.toString())
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
            else -> loadedPhotos
        }

        _photos.value = Resource.Success(sortedPhotos)
    }

    fun selectedPhoto(photo: Photo) {
        _selectedPhoto.value = photo
    }
}