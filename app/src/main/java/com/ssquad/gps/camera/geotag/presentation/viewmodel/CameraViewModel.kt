package com.ssquad.gps.camera.geotag.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
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
import com.ssquad.gps.camera.geotag.data.models.TemplateDataModel
import com.ssquad.gps.camera.geotag.domain.CameraRepository
import com.ssquad.gps.camera.geotag.domain.MapLocationRepository
import com.ssquad.gps.camera.geotag.domain.MediaRepository
import com.ssquad.gps.camera.geotag.domain.WeatherRepository
import com.ssquad.gps.camera.geotag.presentation.mainscreen.activity.CameraState
import com.ssquad.gps.camera.geotag.utils.LocationResult
import com.ssquad.gps.camera.geotag.utils.Resource
import com.ssquad.gps.camera.geotag.utils.formatCaptureDuration
import com.ssquad.gps.camera.geotag.utils.formatToDate
import com.ssquad.gps.camera.geotag.utils.formatToTime
import com.ssquad.gps.camera.geotag.worker.CacheDataTemplate
import com.google.android.material.snackbar.Snackbar
import com.ssquad.gps.camera.geotag.utils.rotate
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
    private val locationRepository: MapLocationRepository,
    private val cameraRepository: CameraRepository,
    private val mediaRepository:MediaRepository,
    private val cacheDataTemplate: CacheDataTemplate,
) : ViewModel() {
    private val _cameraState = MutableStateFlow(CameraState())
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    var camera: Camera? = null
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
    private var countDownJob: Job? = null
    private var tempFile: File? = null
    private var second = 0L

    fun updateCameraState(update: (CameraState) -> CameraState) {
        return _cameraState.update(update)
    }

    override fun onCleared() {
        super.onCleared()
        cacheDataTemplate.cleanup()
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

    private fun bindCameraUseCase(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        val cameraProvider = this.cameraProvider ?: throw IllegalStateException("Chưa khởi tạo camera")

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
    fun toggleFlashDuringRecording(){
        val isTorchOn = camera?.cameraInfo?.torchState?.value == TorchState.ON
        camera?.cameraControl?.enableTorch(!isTorchOn)
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

    private fun startRecordingTime() {
        second = SystemClock.elapsedRealtime()
        recordingTimerJob = viewModelScope.launch {
            while (isActive) {
                val elapsed = SystemClock.elapsedRealtime() - second
                updateCameraState {
                    it.copy(
                        recordingDuration = elapsed.toInt().formatCaptureDuration()
                    )
                }
                delay(1000)
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

    fun setCaptureTime(timerSecond: Int) {
        updateCameraState {
            it.copy(
                selectedTimerDuration = timerSecond
            )
        }
    }

    fun startCaptureCountDown() {
        val timerDuration = _cameraState.value.selectedTimerDuration
        if (timerDuration <= 0) {
            capturePhoto()
            return
        }
        countDownJob?.cancel()
        countDownJob = viewModelScope.launch() {
            try {
                for (i in timerDuration downTo 1) {
                    updateCameraState {
                        it.copy(
                            countDownTimer = i,
                            isCountDown = true
                        )
                    }
                    delay(1000)
                }
                updateCameraState { it.copy(countDownTimer = 0, isCountDown = false) }//reset
                capturePhoto()
            } catch (e: Exception) {
                updateCameraState {
                    it.copy(
                        error = "Lỗi đếm ngược: ${e.message}",
                        isCountDown = false,
                        countDownTimer = 0
                    )
                }
            }
        }
    }

    fun cancelCountDown() {
        countDownJob?.cancel()
        countDownJob = null
        updateCameraState {
            it.copy(
                isCountDown = false,
                countDownTimer = 0
            )
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
                            val rotatedBitmap = bitmap.rotate(image.imageInfo.rotationDegrees)
                            updateCameraState {
                                it.copy(
                                    captureImageBitmap = rotatedBitmap
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
        tempFile?.delete()
        try {
            tempFile = File(
                context.cacheDir,
                "video_${
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                }.mp4"
            )
            Log.d(
                "CameraViewModel",
                "Video file path: ${tempFile?.absolutePath}"
            )
            val fileOutputOptions = FileOutputOptions.Builder(tempFile!!).build()
            recording = videoCapture.output.prepareRecording(context, fileOutputOptions)
                .apply {
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
                                    it.copy(error = "Không tạo được video: " + recordEvent.error)
                                }
                            } else {
                                viewModelScope.launch {
                                    try {
                                        updateCameraState {
                                            it.copy(
                                                isLoading = true, error = null
                                            )
                                        }
                                        updateCameraState {
                                            it.copy(showProcessingSnackbar = true)
                                        }
                                        // Lấy tham chiếu đến template view
                                        val templateView = _cameraState.value.templateView
                                        val savedUri = if (templateView != null) {
                                            // Xử lý video với template
                                            cameraRepository.processVideoWithTemplate(
                                                context,
                                                Uri.fromFile(tempFile),
                                                templateView
                                            )
                                        } else {
                                            // Fallback nếu không có template view
                                            cameraRepository.saveVideoToGallery(
                                                context,
                                                Uri.fromFile(tempFile)
                                            )
                                        }
                                        Log.d(
                                            "CameraViewModel",
                                            "Luu video: $savedUri"
                                        )
                                        updateCameraState {
                                            it.copy(
                                                previewUri = savedUri,
                                                isLoading = false,
                                                showProcessingSnackbar = false,
                                                showSuccessSnackbar = true,
                                                error = if (savedUri == null) "Lưu video không thành công" else null
                                            )
                                        }
                                        delay(3000)
                                        updateCameraState {
                                            it.copy(showSuccessSnackbar = false)
                                        }
                                    } catch (e: Exception) {
                                        updateCameraState {
                                            it.copy(
                                                error = "Lưu video không thành công: ${e.message}",
                                                isLoading = false,
                                                showProcessingSnackbar = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        } catch (e: SecurityException) {
            updateCameraState {
                it.copy(
                    error = "Không có quyền truy cập camera:${e.message}",
                    isRecording = false,
                    isLoading = false
                )
            }
        } catch (e: IllegalStateException) {
            updateCameraState {
                it.copy(
                    error = "Không thể bắt đầu quay video:${e.message}",
                    isRecording = false,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            updateCameraState {
                it.copy(error = e.message, isRecording = false, isLoading = false)
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

        val now = Date()
        if (cacheDataTemplate.isCacheValid()) {
            cacheDataTemplate.templateData.value?.let { cacheData ->
                updateCameraState {
                    it.copy(
                        templateData = TemplateDataModel(
                            location = cacheData.location,
                            lat = cacheData.lat,
                            long = cacheData.long,
                            temperatureC = cacheData.temperatureC,
                            temperatureF = cacheData.temperatureF,
                            currentTime = now.formatToTime(),
                            currentDate = now.formatToDate()
                        )
                    )
                }
            }
        }
        if (!cacheDataTemplate.isCacheValid()) {
            viewModelScope.launch {
                try {
                    var location: String? = null
                    var lat: String? = null
                    var lon: String? = null
                    var temperatureC: Float? = null
                    var temperatureF: Float? = null

                    val locationResult = locationRepository.getCurrentLocation()


                    when (locationResult) {
                        is LocationResult.Success -> {
                            val currentLocation = locationResult.location
                            lat =
                                String.format(Locale.getDefault(), "%.6f", currentLocation.latitude)
                            lon = String.format(
                                Locale.getDefault(),
                                "%.6f",
                                currentLocation.longitude
                            )

                            val addressDeferred = async {
                                locationRepository.getAddressFromLocation(currentLocation)
                            }
                            val fakeTempResult = weatherRepository.getFakeTemp()
                            val tempResult = weatherRepository.getCurrentTemp(currentLocation)
                            val addressResult = addressDeferred.await()
                            if (addressResult is LocationResult.Address) {
                                location = addressResult.address
                            }

                            val tempPair = (if(tempResult is Resource.Success) tempResult.data else null)
                                ?: (if(fakeTempResult is Resource.Success) fakeTempResult.data else null)
                                ?: Pair(null, null)

                            temperatureC = tempPair.first
                            temperatureF = tempPair.second
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
                                temperatureC = temperatureC,
                                temperatureF = temperatureF,
                                currentTime = now.formatToTime(),
                                currentDate = now.formatToDate()
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
    fun getLastCaptureImage(){
        viewModelScope.launch {
            try {
                val uri = mediaRepository.getLatestPhotoInAlbum()
                updateCameraState {
                    it.copy(
                        lastCaptureImage = uri
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
            cancelCountDown()
        } catch (e: Exception) {
            updateCameraState {
                it.copy(
                    error = "Giải phóng không thành công:${e.message}"
                )
            }
        }
    }
}
