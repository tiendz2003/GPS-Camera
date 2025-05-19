package com.ssquad.gps.camera.geotag.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ssquad.gps.camera.geotag.data.local.dao.PhotoDao
import com.ssquad.gps.camera.geotag.data.models.PhotoDto
import com.ssquad.gps.camera.geotag.utils.Converters

@Database(entities = [PhotoDto::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
}