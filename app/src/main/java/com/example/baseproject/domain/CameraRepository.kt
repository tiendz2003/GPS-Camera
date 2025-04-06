package com.example.baseproject.domain

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri

interface CameraRepository {
    suspend fun saveImageToGallery(context: Context, bitmap: Bitmap): Uri?
    suspend fun saveVideoToGallery(context: Context,sourceUri: Uri): Uri?
}