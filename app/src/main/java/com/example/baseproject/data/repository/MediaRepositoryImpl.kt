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
                //MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DISPLAY_NAME
            )
            val selection = "${MediaStore.Images.Media.BUCKET_ID} = ?"
            val selectionArgs = arrayOf(albumId)
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            val query = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                while (cursor.moveToNext()){
                    val id = cursor.getLong(idColumn)
                    val contentUri  = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    photos.add(
                        Photo(
                            id = id,
                            path = contentUri,
                            dateAdded = dateAdded,
                            albumId = albumId,
                            size = size,
                            name = name
                        )
                    )
                }
            }
            photos
        }

    override suspend fun getVideoFromAlbum(albumId: String): List<Photo> =
        withContext(Dispatchers.IO){
            val videos = mutableListOf<Photo>()
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                //MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
            )
            val selection = "${MediaStore.Video.Media.BUCKET_ID} = ?"
            val selectionArgs = arrayOf(albumId)
            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

            val query = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                while (cursor.moveToNext()){
                    val id = cursor.getLong(idColumn)
                    val contentUri  = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val size = cursor.getLong(sizeColumn)
                    val name = cursor.getString(nameColumn)
                    val duration = cursor.getLong(durationColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    videos.add(
                        Photo(
                            id = id,
                            path = contentUri,
                            dateAdded = dateAdded,
                            albumId = albumId,
                            size = size,
                            name = name,
                            duration = duration,
                            isVideo = true
                        )
                    )
                }
            }
            videos
        }
}