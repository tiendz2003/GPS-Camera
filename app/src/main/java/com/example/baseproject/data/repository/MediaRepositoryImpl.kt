package com.example.baseproject.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.baseproject.data.models.Album
import com.example.baseproject.data.models.Photo
import com.example.baseproject.domain.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepositoryImpl (private val context: Context): MediaRepository {
    override suspend fun getAlbums(): List<Album> =
        withContext(Dispatchers.IO) {
                val albums = mutableListOf<Album>()
                val projection = arrayOf(
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media._ID,
                )
                val selection = null
                val selectionArgs = null
                val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} ASC"

                val query = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )
                val bucketIds = mutableSetOf<String>()

                query?.use { cursor ->
                    val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                    val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

                    while (cursor.moveToNext()){
                        val bucketId = cursor.getString(bucketIdColumn)
                        val bucketName = cursor.getString(bucketNameColumn)
                        if(bucketIds.contains(bucketId)){
                            continue
                        }
                        bucketIds.add(bucketId)
                        val countQuery = context.contentResolver.query(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            arrayOf(MediaStore.Images.Media._ID),
                            "${MediaStore.Images.Media.BUCKET_ID} = ?",
                            arrayOf(bucketId),
                            null
                        )
                        val count = countQuery?.count ?: 0
                        countQuery?.close()

                        val id = cursor.getLong(idColumn)
                        val contentUri  = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        albums.add(
                            Album(
                                photoCount = count,
                                id = bucketId,
                                name = bucketName,
                                coverPath = contentUri
                            )
                        )
                    }
                }
                albums
        }

    override suspend fun getPhotosFromAlbum(albumId: String): List<Photo> =
        withContext(Dispatchers.IO){
            val photos = mutableListOf<Photo>()
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_TAKEN,
            )
            val selection = "${MediaStore.Images.Media.BUCKET_ID} = ?"
            val selectionArgs = arrayOf(albumId)
            val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

            val query = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                while (cursor.moveToNext()){
                    val id = cursor.getLong(idColumn)
                    val dateTaken = cursor.getLong(dateTakenColumn)
                    val contentUri  = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    photos.add(
                        Photo(
                            id = id,
                            path = contentUri,
                            dateAdded = dateTaken,
                            albumId = albumId
                        )
                    )
                }
            }
            photos
        }

}