package com.example.baseproject.data.models

data class Album(
    val id: String,
    val name: String,
    val coverPath: String,
    val photoCount: Int
)
data class Photo(
    val id: Long,
    val path: String,
    val albumId: String,
    val dateAdded: Long
)