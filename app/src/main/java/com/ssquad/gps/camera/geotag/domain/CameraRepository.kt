package com.ssquad.gps.camera.geotag.domain

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import okhttp3.Address

interface CameraRepository {
    suspend fun saveImageToGallery(context: Context, bitmap: Bitmap,address: String?): Uri?
    suspend fun processVideoWithTemplate(inputUri: Uri, templateView: View?, address: String?): Uri?
    suspend fun exportTemplateToImage(view: View): String?
    suspend fun saveVideoToGallery(inputUri: Uri, address: String?): Uri?
}