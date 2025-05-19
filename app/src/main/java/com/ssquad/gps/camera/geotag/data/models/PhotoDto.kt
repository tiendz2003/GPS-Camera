package com.ssquad.gps.camera.geotag.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoDto(
    @PrimaryKey val id: Long = 0,
    val path: String,
    val timestamp: Long,
    val source: PhotoSource,
    val displayName: String,
    val isShownInWidget: Boolean = true
)

enum class PhotoSource {
    AUTO,MANUAL
}