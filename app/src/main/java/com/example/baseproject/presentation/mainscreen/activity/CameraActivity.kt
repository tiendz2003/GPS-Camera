package com.example.baseproject.presentation.mainscreen.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.baseproject.R
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.data.models.TemplateDataModel
import com.example.baseproject.databinding.ActivityCameraBinding
import com.example.baseproject.presentation.viewmodel.CameraViewModel
import com.example.baseproject.service.MapManager
import com.example.baseproject.utils.BitmapHolder
import com.example.baseproject.utils.Config
import com.example.baseproject.utils.PermissionManager
import com.example.baseproject.utils.SharePrefManager
import com.example.baseproject.utils.addTemplate
import com.example.baseproject.utils.gone
import com.example.baseproject.utils.invisible
import com.example.baseproject.utils.startCountdownAnimation
import com.example.baseproject.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class CameraActivity : BaseActivity<ActivityCameraBinding>(ActivityCameraBinding::inflate) {
    private val cameraViewModel: CameraViewModel by viewModel()
    private val cameraPermission =
        arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                startCamera()
            } else {
                Toast.makeText(this, "Cần cấp quyền", Toast.LENGTH_SHORT).show()
            }
        }
    private var templateId: String? = null
    private var mapSnapshotJob: Job? = null
    private lateinit var mapManager: MapManager
    var mapSnapshot: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding.mapView.onCreate(savedInstanceState)

        mapManager = MapManager(this, lifecycle, binding.mapView)

        mapManager.setOnMapReadyCallback { map ->
            cameraViewModel.updateTemplateData()
        }
    }

    override fun initData() {
        templateId = SharePrefManager.getDefaultTemplate()
    }

    override fun initView() {
        if (PermissionManager.hasPermissions(this, cameraPermission)) {
            startCamera()
        } else {
            PermissionManager.requestPermissions(requestCameraPermissionLauncher, cameraPermission)
        }

        val savedTimer = SharePrefManager.getTimerPref()
        cameraViewModel.updateCameraState {
            it.copy(
                selectedTimerDuration = savedTimer
            )
        }
        updateTimerIcon(savedTimer)
        updateCameraMode(false)
        observeViewModel()
    }

    override fun initActionView() {
        with(binding) {
            imvBack.setOnClickListener {
                finish()
            }
            imvGird.setOnClickListener {
                cameraViewModel.enableGrid()
                gridOverlay.visibility =
                    if (cameraViewModel.isGridEnabled()) View.VISIBLE else View.GONE
            }

            imvSwitchCamera.setOnClickListener {
                toggleCamera()
            }
            binding.motionLayoutMode.setTransitionListener(object : MotionLayout.TransitionListener {
                override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {}

                override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float) {}

                override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                    when (currentId) {
                        R.id.photo_mode -> {
                            binding.imvTakeCapture.setImageResource(R.drawable.ic_take_photo)

                            binding.tvFunction.setBackgroundResource(R.drawable.bg_btn_photo)
                            binding.tvFunction.setTextColor(Color.BLACK)

                            binding.tvOption.setBackgroundColor(Color.TRANSPARENT)
                            binding.tvOption.setTextColor(getResources().getColor(R.color.neutralGrey))

                            binding.tvDurationVideo.visibility = View.GONE
                        }

                        R.id.video_mode -> {
                            binding.imvTakeCapture.setImageResource(R.drawable.ic_take_photo)

                            binding.tvOption.setBackgroundResource(R.drawable.bg_btn_photo)
                            binding.tvOption.setTextColor(Color.BLACK)

                            binding.tvFunction.setBackgroundColor(Color.TRANSPARENT)
                            binding.tvFunction.setTextColor(getResources().getColor(R.color.neutralGrey))
                        }
                    }
                }

                override fun onTransitionTrigger(motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float) {}
            })

            tvFunction.setOnClickListener {
                Log.d("CameraActivity", "tvFunction clicked!")
                if (cameraViewModel.isVideoMode()) {
                    binding.motionLayoutMode.transitionToState(R.id.photo_mode)
                    cameraViewModel.toggleCameraMode()
                }
            }
            tvOption.setOnClickListener {
                Log.d("CameraActivity", "tvOption clicked!")
                if (!cameraViewModel.isVideoMode()) {
                    binding.motionLayoutMode.transitionToState(R.id.video_mode)
                    cameraViewModel.toggleCameraMode()
                }
            }
            imvTakeCapture.setOnClickListener {
                if (cameraViewModel.isVideoMode()) {
                    Log.d("CameraActivity", "Video mode")
                    toggleVideoRecording()
                } else {
                    Log.d("CameraActivity", "camera mode")
                    takePicture()
                }
            }
            imvFlash.setOnClickListener {
                if (cameraViewModel.currentLensFacing() == CameraSelector.LENS_FACING_FRONT) {
                    Toast.makeText(
                        this@CameraActivity,
                        getString(R.string.front_camera_does_not_support_flash),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                toggleFlashMode()
                updateFlashIcon(cameraViewModel.getCurrentFlashMode())
            }
            imvTimer.setOnClickListener {
                setCountDownTimer()
            }
            imvFullScreen.setOnClickListener {
                cameraViewModel.toggleFullScreen()
                if (cameraViewModel.getCurrentScreenState()) {
                    binding.clRoot.transitionToEnd()
                    binding.imvFullScreen.setImageResource(R.drawable.ic_full_exit)
                    binding.flCamera.elevation = -1f
                    binding.clHeader.elevation = 10f
                    binding.tvCountDown.elevation = 10f
                    binding.clBottom.elevation = 10f
                } else {
                    binding.clRoot.transitionToStart()
                    binding.imvFullScreen.setImageResource(R.drawable.ic_full)
                    binding.flCamera.elevation = 0f
                    binding.tvCountDown.elevation = 0f
                    binding.clHeader.elevation = 0f
                    binding.clBottom.elevation = 0f
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cameraViewModel.cameraState.collect { cameraState ->
                    cameraState.captureImageBitmap?.let { bitmap ->
                        Log.d("CameraActivity", "observeViewModel: $bitmap")
                        navigateToPreviewImage(bitmap)
                    }

                    cameraState.error?.let {
                        Toast.makeText(this@CameraActivity,
                            getString(R.string.error_taking_photo_please_try_again), Toast.LENGTH_SHORT).show()
                    }
                    cameraState.recordingDuration?.let { duration ->
                        binding.tvDurationVideo.text = duration
                    }
                    updateRecordingUI(cameraState.isRecording)
                    if (cameraState.isCountDown && cameraState.countDownTimer > 0) {
                        Log.d("CountingDown", "observeViewModel: ${cameraState.countDownTimer}")
                        binding.tvCountDown.text = "${cameraState.countDownTimer}"
                        binding.tvCountDown.visible()
                        startCountdownAnimation(binding.tvCountDown)
                    } else {
                        binding.tvCountDown.gone()
                    }
                    cameraState.templateData?.let {
                        Log.d("CameraActivity", "observeViewModel: $it")
                        initTemplate(it)
                    }
                }
            }
        }
    }

    private fun navigateToPreviewImage(imgBitmap: Bitmap) {
        val intent = Intent(this, PreviewImageActivity::class.java).apply {
            BitmapHolder.imageBitmap = imgBitmap
            putExtra("TEMPLATE_DATA", cameraViewModel.cameraState.value.templateData)
            putExtra("TEMPLATE_ID", cameraViewModel.cameraState.value.selectedTemplateId)
            putExtra("FROM_ALBUM", false)
            putExtra("IS_FRONT_CAMERA", checkCurrentLensFacing())
        }
        cameraViewModel.updateCameraState {
            it.copy(
                captureImageBitmap = null
            )
        }
        startActivity(intent)
    }
    fun checkCurrentLensFacing(): Boolean {
        return cameraViewModel.currentLensFacing() == CameraSelector.LENS_FACING_FRONT
    }
    private fun startCamera() {
        cameraViewModel.initializeCamera(this, binding.previewView, this)
    }

    private fun takePicture() {
        cameraViewModel.startCaptureCountDown()
    }

    private fun toggleCamera() {
        cameraViewModel.toggleCamera(binding.previewView, this)
    }

    private fun toggleFlashMode() {
        cameraViewModel.toggleFlashMode(binding.previewView, this)
    }

    private fun toggleVideoRecording() {
        cameraViewModel.toggleVideoRecording(this)
        updateRecordingUI(cameraViewModel.cameraState.value.isRecording)
    }

    private fun updateFlashIcon(flashMode: Int) {
        val flashIcon = when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> R.drawable.ic_flash_off
            ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
            else -> R.drawable.ic_flash_off
        }
        binding.imvFlash.setImageResource(flashIcon)
    }

    private fun setCountDownTimer() {
        val currentTimerValue = cameraViewModel.cameraState.value.selectedTimerDuration
        val newTimerValue = when (currentTimerValue) {
            0 -> 3
            3 -> 5
            5 -> 10
            else -> 0
        }

        SharePrefManager.setTimerPref(newTimerValue)
        cameraViewModel.setCaptureTime(newTimerValue)
        updateTimerIcon(newTimerValue)
    }

    private fun updateTimerIcon(timerValue: Int) {
        val iconRes = when (timerValue) {
            0 -> R.drawable.ic_time
            3 -> R.drawable.ic_time_3s
            5 -> R.drawable.ic_time_5s
            10 -> R.drawable.ic_time_10s
            else -> R.drawable.ic_time
        }
        binding.imvTimer.setImageResource(iconRes)
    }

    private fun initTemplate(template: TemplateDataModel) {
        mapSnapshotJob?.cancel()
        cameraViewModel.selectedTemplate(templateId)
        val isGpsTemplate = Config.isGPSTemplate(templateId)
        if (isGpsTemplate) {
            if(cameraViewModel.cameraState.value.isRecording && mapSnapshot != null ){
                updateTemplateOverlay(template, mapSnapshot)
                cameraViewModel.updateCameraState {
                    it.copy(templateView = binding.templateOverlayContainer)
                }
                return
            }
            try {
                val lat = template.lat?.replace(",", ".")?.toDouble()
                val lon = template.long?.replace(",", ".")?.toDouble()
                if (lat == null || lon == null) {
                    Log.e("CameraActivity", "Toạ độ null")
                    return
                }
                mapSnapshotJob = lifecycleScope.launch {
                    delay(500)
                    if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        return@launch
                    }
                    withContext(Dispatchers.Main) {
                        mapManager.captureMapImage(lat, lon) { bitmap ->
                            if (isFinishing || isDestroyed) return@captureMapImage
                            mapSnapshot = bitmap
                            Log.d("CameraActivity", "Map snapshot captured: ${bitmap != null}")
                            updateTemplateOverlay(template, bitmap)
                            cameraViewModel.updateCameraState {
                                it.copy(templateView = binding.templateOverlayContainer)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CameraActivity", "Error processing location data: ${e.message}")
                updateTemplateOverlay(template,null)
            }
        } else {
            updateTemplateOverlay(template,null)
            cameraViewModel.updateCameraState {
                it.copy(templateView = binding.templateOverlayContainer)
            }
        }
    }
    fun updateTemplateOverlay(template: TemplateDataModel,bitmap: Bitmap?) {
        if(bitmap != null) {
            binding.templateOverlayContainer.addTemplate(
                this,
                templateId ?: Config.TEMPLATE_1,
                template,
                null,
                bitmap
            )
        } else {
            binding.templateOverlayContainer.addTemplate(
                this,
                templateId ?: Config.TEMPLATE_1,
                template,
                null,
                null
            )
        }
    }
    private fun updateCameraMode(isVideoMode: Boolean) {
        with(binding) {
            if (isVideoMode) {
                tvFunction.setBackgroundResource(android.R.color.transparent)
                tvFunction.setTextColor(Color.WHITE)

                tvOption.setBackgroundResource(R.drawable.bg_btn_photo)
                tvOption.setTextColor(Color.BLACK)

                imvTakeCapture.setImageResource(R.drawable.ic_record_video)
            } else {
                tvFunction.setBackgroundResource(R.drawable.bg_btn_photo)
                tvFunction.setTextColor(Color.BLACK)

                tvOption.setBackgroundResource(android.R.color.transparent)
                tvOption.setTextColor(Color.WHITE)

                imvTakeCapture.setImageResource(R.drawable.ic_take_photo)
            }
        }
    }

    private fun updateRecordingUI(isRecording: Boolean) {
        with(binding) {
            if (isRecording) {
                imvTakeCapture.setImageResource(R.drawable.ic_record_video)
                tvDurationVideo.visible()
                tvFunction.invisible()
                tvOption.invisible()
                imvSelectImage.invisible()
                imvOpenTemplate.invisible()
            } else {
                imvTakeCapture.setImageResource(R.drawable.ic_take_photo)
                tvDurationVideo.gone()
                tvFunction.visible()
                tvOption.visible()
                imvSelectImage.visible()
                imvOpenTemplate.visible()
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraViewModel.cancelCountDown()
        cameraViewModel.cleanupCamera()
        mapSnapshotJob?.cancel()
        mapSnapshot = null
    }
}