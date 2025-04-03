package com.example.baseproject.data.models

import android.net.Uri

data class Album(
    val id: String,
    val name: String,
    val coverPath: Uri,
    val photoCount: Int
)
data class Photo(
    val id: Long,
    val path: Uri,
    val albumId: String,
    val dateAdded: Long
)