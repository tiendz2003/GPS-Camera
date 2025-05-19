package com.ssquad.gps.camera.geotag.utils

import androidx.room.TypeConverter
import com.ssquad.gps.camera.geotag.data.models.PhotoSource

class Converters {
    @TypeConverter
    fun fromPhotoSource(value: PhotoSource): String = value.name

    @TypeConverter
    fun toPhotoSource(value: String): PhotoSource = PhotoSource.valueOf(value)
}