package com.ssquad.gps.camera.geotag.presentation.mainscreen.activity

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import com.ssquad.gps.camera.geotag.data.models.Photo
import com.ssquad.gps.camera.geotag.data.models.TemplateDataModel

data class CameraState(
    val isLoading : Boolean = false,
    val captureImageBitmap:Bitmap ?= null,
    val previewUri : Uri?= null,
    val countDownTimer:Int = 0,
    val selectedTimerDuration: Int= 0 ,
    val isCameraReady : Boolean = false,
    val isVideoMode : Boolean = false,
    val isRecording : Boolean = false,
    val isCountDown : Boolean = false,
    val recordingDuration: String? = null,
    val error : String? = null,
    val selectedTemplateId: String? = null,
    val templateData: TemplateDataModel? = null,
    val lastCaptureImage:Photo? =null,
    val templateView: View? = null,
    val showProcessingSnackbar: Boolean = false,
    val showSuccessSnackbar: Boolean = false
)