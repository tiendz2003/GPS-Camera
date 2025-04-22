package com.ssquad.gps.camera.geotag.domain

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.View

interface CameraRepository {
    suspend fun saveImageToGallery(context: Context, bitmap: Bitmap): Uri?
    suspend fun saveVideoToGallery(context: Context, sourceUri: Uri): Uri?
    suspend fun processVideoWithTemplate(context: Context, videoUri: Uri, templateView: View): Uri?
}