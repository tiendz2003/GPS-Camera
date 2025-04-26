package com.ssquad.gps.camera.geotag.data.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class Album(
    val id: String,
    val name: String,
    val coverPath: Uri,
    val photoCount: Int
)
@Parcelize
data class Photo(
    val id: Long,
    val path: Uri,
    val dateAdded: Long,
    val albumId: String,
    val size: Long,
    val name: String,
    val duration: Long = 0,
    val isVideo: Boolean = false,
    val locationAddress: String? = null
): Parcelable
enum class SortOption(val displayName: String) {
    NAME("Name"),
    FILE_SIZE("File size"),
    DATE_ADDED("Date added")
}