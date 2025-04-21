package com.example.baseproject.domain

import android.net.Uri
import com.example.baseproject.data.models.Album
import com.example.baseproject.data.models.Photo

interface MediaRepository {
    suspend fun getAlbums(): List<Album>
    suspend fun getPhotosFromAlbum(albumId: String): List<Photo>
    suspend fun getVideoFromAlbum(albumId: String): List<Photo>
    suspend fun getLatestPhotoInAlbum(): Photo?
}