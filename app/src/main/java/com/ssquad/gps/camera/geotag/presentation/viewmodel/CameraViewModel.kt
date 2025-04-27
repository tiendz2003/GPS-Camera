package com.ssquad.gps.camera.geotag.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.camera.core.UseCaseGroup
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import com.ssquad.gps.camera.geotag.utils.parcelable
import com.ssquad.gps.camera.geotag.utils.rotate
import com.ssquad.gps.camera.geotag.service.VideoProcessingService
import kotlinx.coroutines.Job
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
    private val mediaRepository: MediaRepository,
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
    private var videoSavedReceiver: BroadcastReceiver? = null

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
        val cameraProvider =
            this.cameraProvider ?: throw IllegalStateException("Chưa khởi tạo camera")

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

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)

        try {
            cameraProvider.unbindAll()
            val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(preview!!)
                .addUseCase(videoCapture!!)
                .addUseCase(imageCapture!!)

            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                useCaseGroup.build()
            )
        } catch (e: Exception) {
            Log.d("CameraViewModel", ": ${e.message}")
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

    fun toggleFlashDuringRecording() {
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

    fun startVideoRecording(context: Context) {
        val videoCapture = this.videoCapture ?: return
        tempFile?.delete()
        try {
            tempFile = File(
                context.cacheDir,
                "video_${
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                }.mp4"
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
                                handleRecordingFinished(context)
                            }
                        }
                    }
                }
        } catch (e: SecurityException) {
            updateCameraState {
                it.copy(
                    error = "Không thể quay video: ${e.message}"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Không thể bắt đầu quay video", e)
        }
    }

    private fun handleRecordingFinished(context: Context) {
        viewModelScope.launch {
            try {
                updateCameraState {
                    it.copy(
                        isLoading = true,
                        error = null
                    )
                }

                registerVideoSavedReceiver(context)

                // Xử lý template nếu có
                val templatePath = _cameraState.value.templateView?.let { templateView ->
                    cameraRepository.exportTemplateToImage(templateView)
                }

                // Khởi động service để xử lý video
                VideoProcessingService.startProcessing(
                    context,
                    Uri.fromFile(tempFile),
                    _cameraState.value.templateData?.location,
                    templatePath
                )


            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi chuẩn bị xử lý video", e)
                updateCameraState {
                    it.copy(
                        isLoading = false,
                        error = "Lỗi: ${e.message}"
                    )
                }
            }
        }
    }

    private fun registerVideoSavedReceiver(context: Context) {
        // Hủy receiver cũ nếu có
        unregisterVideoSavedReceiver(context)

        // Đăng ký receiver mới
        videoSavedReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    VideoProcessingService.ACTION_VIDEO_SAVED -> {
                        val savedUri = intent.parcelable<Uri>(VideoProcessingService.EXTRA_SAVED_URI)
                        updateCameraState {
                            it.copy(
                                isLoading = false,
                                previewUri = savedUri,
                                showSuccessSnackbar = true
                            )
                        }
                    }

                    VideoProcessingService.ACTION_VIDEO_SAVE_FAILED -> {
                        val errorMsg = intent.getStringExtra(
                            VideoProcessingService.EXTRA_ERROR_MESSAGE
                        ) ?: "Xử lý video thất bại"

                        updateCameraState {
                            it.copy(
                                isLoading = false,
                                error = errorMsg,
                            )
                        }
                    }
                }
            }
        }

        LocalBroadcastManager.getInstance(context).registerReceiver(
            videoSavedReceiver!!,
            IntentFilter().apply {
                addAction(VideoProcessingService.ACTION_VIDEO_SAVED)
                addAction(VideoProcessingService.ACTION_VIDEO_SAVE_FAILED)
            }
        )
    }

    private fun unregisterVideoSavedReceiver(context: Context) {
        videoSavedReceiver?.let {
            try {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering receiver", e)
            }
        }
        videoSavedReceiver = null
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
        val customDate = SharePrefManager.getString("CUSTOM_DATE", "") ?: now.formatToDate()
        val customTime = SharePrefManager.getString("CUSTOM_TIME", "") ?: now.formatToTime()
        val selectedOption = SharePrefManager.getString("DATE_TIME_OPTION", "current")
        Log.d("PhotosViewModel", "CUSTOM_DATE: $customDate")
        Log.d("PhotosViewModel", "CUSTOM_TIME: $customTime")
        Log.d("PhotosViewModel", "DATE_TIME_OPTION: $selectedOption")
        val currentDate =
            if (selectedOption == "custom" && customDate.isNotEmpty() && customTime.isNotEmpty()) {
                customDate
            } else {
                now.formatToDate()
            }

        val currentTime =
            if (selectedOption == "custom" && customDate.isNotEmpty() && customTime.isNotEmpty()) {
                customTime
            } else {
                now.formatToTime()
            }
        Log.d("PhotosViewModel", "CUrrentDATE: $currentDate")
        Log.d("PhotosViewModel", "CUrrent_TIME: $currentTime")
        if (cacheDataTemplate.isCacheValid()) {
            cacheDataTemplate.templateData.value?.let { data ->
                val (lat, long, location) = SharePrefManager.getCachedCoordinates()
                    ?: Triple(
                        data.lat?.replace(",", ".")?.toDouble(),
                        data.long?.replace(",", ".")?.toDouble(),
                        data.location
                    )

                updateCameraState {
                    it.copy(
                        templateData = TemplateDataModel(
                            location = location,
                            lat = lat.toString(),
                            long = long.toString(),
                            temperatureC = data.temperatureC,
                            temperatureF = data.temperatureF,
                            currentTime = currentTime,
                            currentDate = currentDate
                        )
                    )
                }
            }
        }
        if (!cacheDataTemplate.isCacheValid() || cacheDataTemplate.templateData.value == null) {
            viewModelScope.launch {
                try {
                    val locationResult = locationRepository.getCurrentLocation()
                    if (locationResult is LocationResult.Success) {
                        val currentLocation = locationResult.location
                        val lat =
                            String.format(Locale.getDefault(), "%.6f", currentLocation.latitude)
                        val lon =
                            String.format(Locale.getDefault(), "%.6f", currentLocation.longitude)
                        val addressResult =
                            locationRepository.getAddressFromLocation(currentLocation)
                        val tempResult = weatherRepository.getCurrentTemp(currentLocation)
                        val fakeTempResult = weatherRepository.getFakeTemp()

                        val location = if (addressResult is LocationResult.Address) {
                            addressResult.address
                        } else {
                            null
                        }

                        val tempPair =
                            (if (tempResult is Resource.Success) tempResult.data else null)
                                ?: (if (fakeTempResult is Resource.Success) fakeTempResult.data else null)
                                ?: Pair(null, null)

                        val tempC = tempPair.first
                        val tempF = tempPair.second

                        updateCameraState {
                            it.copy(
                                templateData = TemplateDataModel(
                                    location = location,
                                    lat = lat,
                                    long = lon,
                                    temperatureC = tempC,
                                    temperatureF = tempF,
                                    currentTime = currentTime,
                                    currentDate = currentDate
                                )
                            )
                        }

                    }
                } catch (e: Exception) {
                    Log.e("PhotosViewModel", "Error: ${e.message}")
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

    fun getLastCaptureImage() {
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

    companion object {
        private const val TAG = "CameraViewModel"
    }
}
