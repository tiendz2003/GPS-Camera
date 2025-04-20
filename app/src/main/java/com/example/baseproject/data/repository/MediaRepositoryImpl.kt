package com.example.baseproject.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
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
        withContext(Dispatchers.IO) {
            Log.d("MediaRepository", "Đang tìm video trong album: $albumId")
            val videos = mutableListOf<Photo>()
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATA,  // Thêm để debug đường dẫn file
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME  // Thêm để debug tên thư mục
            )

            // Tạo query cụ thể cho các video trong thư mục GPS_CAMERA
            val selection = if (albumId == "GPS_CAMERA") {
                // Phương án 1: Tìm theo đường dẫn chứa "GPS_CAMERA"
                "${MediaStore.Video.Media.DATA} LIKE ?"
            } else {
                // Phương án mặc định: tìm theo BUCKET_ID
                "${MediaStore.Video.Media.BUCKET_ID} = ?"
            }

            val selectionArgs = if (albumId == "GPS_CAMERA") {
                arrayOf("%/DCIM/GPS_CAMERA/%")
            } else {
                arrayOf(albumId)
            }

            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

            try {
                val query = context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )

                Log.d("MediaRepository", "Query executed with: selection=$selection, args=${selectionArgs[0]}")

                query?.use { cursor ->
                    Log.d("MediaRepository", "Tìm thấy ${cursor.count} video")

                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                    val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                    val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                    val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                    val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        val size = cursor.getLong(sizeColumn)
                        val name = cursor.getString(nameColumn)
                        val duration = cursor.getLong(durationColumn)
                        val dateAdded = cursor.getLong(dateAddedColumn)
                        val path = cursor.getString(pathColumn)
                        val bucketName = cursor.getString(bucketNameColumn)

                        // Log để debug
                        Log.d("MediaRepository", "Video found: name=$name, path=$path, bucket=$bucketName")

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
            } catch (e: Exception) {
                Log.e("MediaRepository", "Lỗi khi truy vấn video: ${e.message}", e)
            }

            Log.d("MediaRepository", "Tìm thấy tổng cộng ${videos.size} video")
            videos
        }
}