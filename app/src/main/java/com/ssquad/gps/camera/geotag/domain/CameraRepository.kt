package com.ssquad.gps.camera.geotag.domain

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import okhttp3.Address

interface CameraRepository {
    suspend fun saveImageToGallery(context: Context, bitmap: Bitmap,address: String?): Uri?
    suspend fun saveVideoToGallery(context: Context, sourceUri: Uri,address: String?): Uri?
    suspend fun processVideoWithTemplate(context: Context, videoUri: Uri, templateView: View,  address: String?): Uri?
}