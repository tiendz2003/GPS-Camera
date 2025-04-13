package com.example.baseproject.presentation.viewmodel

import android.annotation.SuppressLint
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
import com.example.baseproject.data.models.TemplateDataModel
import com.example.baseproject.domain.MapLocationRepository
import com.example.baseproject.domain.WeatherRepository
import com.example.baseproject.presentation.mainscreen.activity.CameraState
import com.example.baseproject.utils.LocationResult
import com.example.baseproject.utils.Resource
import com.example.baseproject.utils.formatDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.text.format

class CameraViewModel(
    private val weatherRepository: WeatherRepository,
    private val locationRepository: MapLocationRepository
) : ViewModel() {
    private val _cameraState = MutableStateFlow<CameraState>(CameraState())
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var preview: Preview? = null
    private var cameraExecutor = Executors.newSingleThreadExecutor()

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private var isGridEnabled = false
    private var isRecording = false
    private var isFullScreen = false
    private var isVideoMode = false

    private var recordingTimerJob: Job? = null
    private var tempFile: File? = null



    fun updateCameraState(update: (CameraState) -> CameraState) {
        return _cameraState.update(update)
    }

    override fun onCleared() {
        super.onCleared()
        cleanupCamera()
        cameraExecutor.shutdown()
    }

    fun initializeCamera(
        context: Context,
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner
    ) {
        viewModelScope.launch {
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCase(previewView, lifecycleOwner)
                updateCameraState {
                    it.copy(
                        isCameraReady = false
                    )
                }
            } catch (e: Exception) {
                updateCameraState {
                    it.copy(
                        error = e.message,
                        isCameraReady = false
                    )
                }
            }
        }
    }

    fun bindCameraUseCase(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
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

        } catch (e: Exception) {
            _cameraState.update {
                it.copy(
                    error = e.message
                )
            }
        }
    }

    fun toggleCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        bindCameraUseCase(previewView, lifecycleOwner)
    }

    fun toggleFlashMode(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        flashMode = when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
        imageCapture?.flashMode = flashMode

        bindCameraUseCase(previewView, lifecycleOwner)
    }

    fun toggleFullScreen() {
        isFullScreen = !isFullScreen
    }

    fun toggleCameraMode() {
        isVideoMode = !isVideoMode
        updateCameraState {
            it.copy(
                isVideoMode = isVideoMode
            )
        }
    }

    fun isVideoMode(): Boolean {
        return isVideoMode
    }

    fun startRecordingTime() {
        var second = 0
        recordingTimerJob = viewModelScope.launch {
            while (isActive) {
                updateCameraState {
                    it.copy(
                        recordingDuration = second.formatDuration()
                    )
                }
                delay(1000)
                second++
            }
        }
    }

    private fun stopRecordingTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        updateCameraState {
            it.copy(
                recordingDuration = null
            )
        }
    }

    fun getCurrentScreenState(): Boolean {
        return isFullScreen
    }

    fun enableGrid() {
        isGridEnabled = !isGridEnabled
    }

    fun takePhoto(timerSeconds: Int = 0) {
        if (timerSeconds > 0) {
            countToCapturePhoto(timerSeconds)
        } else {
            capturePhoto()
        }
    }

    private fun countToCapturePhoto(seconds: Int) {
        viewModelScope.launch {
            for (i in seconds downTo 1) {
                updateCameraState {
                    it.copy(
                        countDownTimer = i
                    )
                }
                delay(1000)
            }
            updateCameraState { it.copy(countDownTimer = 0) }//reset
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
                        } catch (e: Exception) {
                            updateCameraState {
                                it.copy(
                                    error = e.message
                                )
                            }
                        } finally {
                            image.close()
                        }
                    }
                }
            }
        )
    }

    fun toggleVideoRecording(context: Context) {
        if (isRecording) {
            stopVideoRecording()
            stopRecordingTimer()
            updateCameraState {
                it.copy(
                    isRecording = false
                )
            }
        } else {
            startVideoRecording(context)
            startRecordingTime()
            updateCameraState {
                it.copy(
                    isRecording = true
                )
            }
        }
    }

    private fun startVideoRecording(context: Context) {
        val videoCapture = this.videoCapture ?: return
        try {
            tempFile = File(
                context.cacheDir,
                "video_${
                    SimpleDateFormat(
                        "yyyyMMdd_HHmmss",
                        Locale.getDefault()
                    ).format(Date())
                }.mp4"
            )
            val fileOutputOptions = FileOutputOptions.Builder(tempFile!!).build()
            recording = videoCapture.output.prepareRecording(
                context, fileOutputOptions
            ).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    withAudioEnabled()
                }
            }
                .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                    when (recordEvent) {
                        is VideoRecordEvent.Start -> {
                            isRecording = true
                        }

                        is VideoRecordEvent.Finalize -> {
                            isRecording = false
                            if (recordEvent.hasError()) {
                                updateCameraState {
                                    it.copy(
                                        error = "Khoong tao duoc video: " + recordEvent.error
                                    )
                                }
                            } else {
                                updateCameraState {
                                    it.copy(
                                        previewUri = Uri.fromFile(tempFile)
                                    )
                                }
                            }
                        }
                    }
                }
        } catch (e: SecurityException) {
            updateCameraState {
                it.copy(
                    error = e.message
                )
            }
        } catch (e: Exception) {
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

    fun selectedTemplate(templateId: String?) {
        updateCameraState {
            it.copy(
                selectedTemplateId = templateId
            )
        }
    }

    @SuppressLint("DefaultLocale")
    fun updateTemplateData() {
        viewModelScope.launch {
            try {
                var location: String? = null
                var lat: String? = null
                var lon: String? = null
                var temperature: String? = null

                val locationDeferred = async {
                    locationRepository.getCurrentLocation()
                }

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val currentDate = dateFormat.format(Date())
                val currentTime = timeFormat.format(Date())

                when (val locationResult = locationDeferred.await()) {
                    is LocationResult.Success -> {
                        val currentLocation = locationResult.location
                        lat = String.format("%.6f", currentLocation.latitude)
                        lon = String.format("%.6f", currentLocation.longitude)

                        val addressDeferred = async {
                            locationRepository.getAddressFromLocation(currentLocation)
                        }
                        val tempDeferred = async {
                            weatherRepository.getCurrentTemp(currentLocation)
                        }

                        val addressResult = addressDeferred.await()
                        if (addressResult is LocationResult.Address) {
                            location = addressResult.address
                        }

                        val tempResult = tempDeferred.await()
                        if (tempResult is Resource.Success) {
                            temperature = tempResult.data
                        } else {
                            val fakeTempDeferred = async(Dispatchers.IO) {
                                weatherRepository.getFakeTemp()
                            }
                            val fakeTemp = fakeTempDeferred.await()
                            if (fakeTemp is Resource.Success) {
                                temperature = fakeTemp.data
                            }
                        }
                    }
                    else -> {
                        //
                    }
                }

                // Cập nhật
                updateCameraState {
                    it.copy(
                        templateData = TemplateDataModel(
                            location = location,
                            lat = lat,
                            long = lon,
                            temperature = temperature,
                            currentTime = currentTime,
                            currentDate = currentDate
                        )
                    )
                }
            } catch (e: Exception) {
                updateCameraState {
                    it.copy(
                        error = e.message
                    )
                }
            }
        }
    }

    fun hasCamera(context: Context, facing: Int): Boolean {
        return try {
            val provider = ProcessCameraProvider.getInstance(context).get()
            val availableCameraInfos = provider.availableConcurrentCameraInfos.filter {
                val cameraSelector = CameraSelector.Builder().requireLensFacing(facing).build()
                provider.hasCamera(cameraSelector)
            }
            availableCameraInfos.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    fun getCurrentFlashMode(): Int {
        return flashMode
    }

    fun isGridEnabled(): Boolean {
        return isGridEnabled
    }

    fun currentLensFacing(): Int {
        return lensFacing
    }

    fun cleanupCamera() {
        try {
            cameraProvider?.unbindAll()
            cameraProvider = null
            camera = null
            preview = null
            imageCapture = null
            videoCapture = null
            recordingTimerJob?.cancel()
            recordingTimerJob = null
            recording = null
        } catch (e: Exception) {
            updateCameraState {
                it.copy(
                    error = "Giari phóng không thành công:${e.message}"
                )
            }
        }
    }
}
