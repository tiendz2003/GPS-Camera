package com.ssquad.gps.camera.geotag.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssquad.gps.camera.geotag.data.models.Album
import com.ssquad.gps.camera.geotag.domain.MediaRepository
import com.ssquad.gps.camera.geotag.utils.Resource
import kotlinx.coroutines.launch

class AlbumViewModel(
    private val repository: MediaRepository
):ViewModel() {
    private val _albums = MutableLiveData<Resource<List<Album>>>()
    val albums: MutableLiveData<Resource<List<Album>>> = _albums

    init {
        loadAlbums()
    }
    fun loadAlbums(){
        viewModelScope.launch {
            _albums.value = Resource.Loading()
            try {
                val result = repository.getAlbums()
                _albums.value = Resource.Success(result)
            }catch (e:Exception){
                Log.e("EditAlbumLibraryActivity", "Error: ${e.message}")
                _albums.value = Resource.Error("Méo tải được: "+e.message.toString())
            }
        }
    }
}