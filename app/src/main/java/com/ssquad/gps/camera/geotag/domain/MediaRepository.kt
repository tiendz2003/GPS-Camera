package com.ssquad.gps.camera.geotag.domain

import com.ssquad.gps.camera.geotag.data.models.Album
import com.ssquad.gps.camera.geotag.data.models.Photo

interface MediaRepository {
    suspend fun getAlbums(): List<Album>
    suspend fun getPhotosFromAlbum(albumId: String): List<Photo>
    suspend fun getVideoFromAlbum(albumId: String): List<Photo>
    suspend fun getLatestPhotoInAlbum(): Photo?
}