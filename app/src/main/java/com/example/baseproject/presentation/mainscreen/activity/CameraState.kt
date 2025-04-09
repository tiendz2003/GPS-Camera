package com.example.baseproject.presentation.mainscreen.activity

import android.graphics.Bitmap
import android.net.Uri
import kotlin.time.Duration

data class CameraState(
    val captureImageBitmap:Bitmap ?= null,
    val previewUri : Uri?= null,
    val countDownTimer:Int = 0,
    val selectedTimerDuration: Int= 0 ,
    val isCameraReady : Boolean = false,
    val isVideoMode : Boolean = false,
    val isRecording : Boolean = false,
    val recordingDuration: String? = null,
    val error : String? = null
)