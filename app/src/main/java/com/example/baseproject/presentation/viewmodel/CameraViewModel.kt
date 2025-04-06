package com.example.baseproject.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject.presentation.mainscreen.activity.CameraState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class CameraViewModel: ViewModel() {
    private val _cameraState = MutableStateFlow<CameraState>(CameraState())
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private var cameraProvider :ProcessCameraProvider ? = null
    private var camera: Camera? = null
    private var imageCapture : ImageCapture? = null
    private var videoCapture:VideoCapture<Recorder> ?= null
    private var recording:Recording ?= null
    private var preview: Preview?= null
    private var cameraExecutor = Executors.newSingleThreadExecutor()

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private var isGridEnabled = false
    private var isRecording = false

    private var tempFile:File? = null
    fun updateCameraState(update: (CameraState) -> CameraState) {
        return _cameraState.update(update)
    }
    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }
    fun initializeCamera(context:Context,previewView: PreviewView,lifecycleOwner: LifecycleOwner){
       viewModelScope.launch {
           try {
               val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
               cameraProvider = cameraProviderFuture.get()
               bindCameraUseCase(context,previewView,lifecycleOwner)
               updateCameraState {
                   it.copy(
                       isCameraReady = false
                   )
               }
           }catch (e:Exception){
               updateCameraState {
                   it.copy(
                       error = e.message,
                       isCameraReady = false
                   )
               }
           }
       }
    }
    fun bindCameraUseCase(context: Context,previewView: PreviewView,lifecycleOwner: LifecycleOwner){
        val cameraProvider = this.cameraProvider ?: throw IllegalStateException("Chuwa khoi tao")

        //cameara selector
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        preview = Preview.Builder()
            .build()
            .also {
                it.surfaceProvider = previewView.surfaceProvider
            }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(flashMode)
            .build()
        //quay vidoe
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)
        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                videoCapture
            )

        }catch (e:Exception){
            _cameraState.update {
                it.copy(
                    error = e.message
                )
            }
        }
    }
    fun toggleCamera(context: Context,previewView: PreviewView,lifecycleOwner: LifecycleOwner){
        lensFacing = if(lensFacing == CameraSelector.LENS_FACING_BACK){
            CameraSelector.LENS_FACING_FRONT
        }else{
            CameraSelector.LENS_FACING_BACK
        }
        bindCameraUseCase(context,previewView,lifecycleOwner)
    }
    fun toggleFlashMode(context: Context,previewView: PreviewView,lifecycleOwner: LifecycleOwner) {
        flashMode = when(flashMode){
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
        imageCapture?.flashMode = flashMode

        bindCameraUseCase(context,previewView,lifecycleOwner)
    }
    fun enableGrid(){
        isGridEnabled = !isGridEnabled
    }
    fun takePhoto(timerSeconds: Int = 0){
        if(timerSeconds > 0){
            countToCapturePhoto(timerSeconds)
        }else{
            capturePhoto()
        }
    }
    private fun countToCapturePhoto(seconds : Int) {
        viewModelScope.launch {
            for(i in seconds downTo 1){
                updateCameraState { it.copy(
                    countDownTimer = i
                )
                }
                delay(1000)
            }
            updateCameraState { it.copy(countDownTimer = 0) }//reset\
            //cười nào
            capturePhoto()
        }

    }
    private fun capturePhoto() {
        val imageCapture = this.imageCapture ?: return
        imageCapture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                   viewModelScope.launch {
                       try {
                           val bitmap = image.toBitmap()
                           updateCameraState {
                               it.copy(
                                   captureImageBitmap = bitmap
                               )
                           }
                       }catch (e:Exception){
                           updateCameraState {
                               it.copy(
                                   error = e.message
                               )
                           }
                       }finally {
                           image.close()
                       }
                   }
                }
            }
        )
    }
    fun toggleVideoRecording(context: Context) {
        if(isRecording){
            stopVideoRecording()
        }else{
            startVideoRecording(context)
        }
    }

    private fun startVideoRecording(context: Context) {
        val videoCapture = this.videoCapture ?: return
        try {
            tempFile = File(
                context.cacheDir,
                "video_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.mp4"
            )
            val fileOutputOptions = FileOutputOptions.Builder(tempFile!!).build()
            recording = videoCapture.output.prepareRecording(
                context,fileOutputOptions
            ).apply {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                    withAudioEnabled()
                }
            }
                .start(ContextCompat.getMainExecutor(context)){recordEvent->
                    when(recordEvent){
                        is VideoRecordEvent.Start -> {
                            isRecording = true
                        }
                        is VideoRecordEvent.Finalize ->{
                            isRecording = false
                            if(recordEvent.hasError()){
                                updateCameraState {
                                    it.copy(
                                        error = "Khoong tao duoc video: "+recordEvent.error
                                    )
                                }
                            }else{
                                updateCameraState {
                                    it.copy(
                                        previewUri = Uri.fromFile(tempFile)
                                    )
                                }
                            }
                        }
                    }
                }
        }catch (e:SecurityException){
            updateCameraState {
                it.copy(
                    error = e.message
                )
            }
        }
        catch (e:Exception){
            updateCameraState {
                it.copy(
                    error = e.message
                )
            }
        }
    }

    private fun stopVideoRecording() {
        recording?.stop()
        recording = null
    }
    fun hasCamera(context: Context,facing:Int):Boolean{
        return try {
            val provider = ProcessCameraProvider.getInstance(context).get()
            val availableCameraInfos = provider.availableConcurrentCameraInfos.filter {
                val cameraSelector = CameraSelector.Builder().requireLensFacing(facing).build()
                provider.hasCamera(cameraSelector)
            }
            availableCameraInfos.isNotEmpty()
        }catch (e:Exception){
            false
        }
        fun getCurrentFlashMode():Int{
            return flashMode
        }
        fun isGridEnabled():Boolean{
            return isGridEnabled
        }
        fun currentLensFacing():Int{
            return lensFacing
        }
    }
}