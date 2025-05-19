package com.ssquad.gps.camera.geotag.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ssquad.gps.camera.geotag.data.models.PhotoDto
import com.ssquad.gps.camera.geotag.data.models.PhotoSource

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE isShownInWidget = 1 AND source = :sourcePhoto ORDER BY timestamp DESC LIMIT 3")
    fun getWidgetPhotosBySource(sourcePhoto: PhotoSource):LiveData<List<PhotoDto>>

    @Query("SELECT * FROM photos WHERE isShownInWidget = 1 AND source = :sourcePhoto ORDER BY timestamp DESC LIMIT 3")
    fun getWidgetPhotoSyncBySource(sourcePhoto: PhotoSource):List<PhotoDto>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoDto)

    @Update
    suspend fun updatePhoto(photo: PhotoDto)
}