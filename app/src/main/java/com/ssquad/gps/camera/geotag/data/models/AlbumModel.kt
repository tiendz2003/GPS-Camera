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
    val name: String,
    val size: Long,
    val path: Uri,
    val albumId: String,
    val dateAdded: Long,
    val duration: Long? = null,
    val isVideo: Boolean = false
): Parcelable
enum class SortOption(val displayName: String) {
    NAME("Name"),
    FILE_SIZE("File size"),
    DATE_ADDED("Date added")
}