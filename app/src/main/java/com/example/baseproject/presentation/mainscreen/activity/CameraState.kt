package com.example.baseproject.presentation.mainscreen.activity

import android.graphics.Bitmap
import android.net.Uri

data class CameraState(
    val captureImageBitmap:Bitmap ?= null,
    val previewUri : Uri?= null,
    val countDownTimer:Int = 0,
    val isCameraReady : Boolean = false,
    val error : String? = null
)