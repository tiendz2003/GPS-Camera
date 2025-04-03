package com.example.baseproject.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject.data.models.Album
import com.example.baseproject.data.models.Photo
import com.example.baseproject.domain.MediaRepository
import com.example.baseproject.utils.Resource
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
                _albums.value = Resource.Error("Méo tải được: "+e.message.toString())
            }
        }
    }



}