package com.example.baseproject.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject.data.models.Photo
import com.example.baseproject.domain.MapLocationRepository
import com.example.baseproject.domain.MediaRepository
import com.example.baseproject.domain.WeatherRepository
import com.example.baseproject.utils.LocationResult
import com.example.baseproject.utils.Resource
import kotlinx.coroutines.launch

class PhotosViewModel(
    private val repository: MediaRepository,
): ViewModel()  {

    private val _photos = MutableLiveData<Resource<List<Photo>>>()
    val photos: LiveData<Resource<List<Photo>>> = _photos
    private val _selectedPhoto = MutableLiveData<Photo>()
    val selectedPhoto: LiveData<Photo> = _selectedPhoto
    fun loadPhotosFromAlbum(albumId: String){
        viewModelScope.launch {
            _photos.value = Resource.Loading()
            try {
                val result = repository.getPhotosFromAlbum(albumId)
                Log.d("EditAlbumLibraryActivity", "observeViewModel: $result")
                _photos.value = Resource.Success(result)
            }catch (e:Exception){
                _photos.value = Resource.Error("Méo tải được: "+e.message.toString())
            }
        }
    }
    fun selectedPhoto(photo: Photo) {
        _selectedPhoto.value = photo
    }
}