package com.example.baseproject.presentation.mainscreen.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.baseproject.R
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.data.models.TemplateDataModel
import com.example.baseproject.databinding.ActivityCameraBinding
import com.example.baseproject.presentation.viewmodel.CameraViewModel
import com.example.baseproject.utils.BitmapHolder
import com.example.baseproject.utils.Config
import com.example.baseproject.utils.PermissionManager
import com.example.baseproject.utils.SharePrefManager
import com.example.baseproject.utils.addTemplate
import com.example.baseproject.utils.gone
import com.example.baseproject.utils.invisible
import com.example.baseproject.utils.startCountdownAnimation
import com.example.baseproject.utils.visible
import kotlinx.coroutines.launch
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun initData() {
        cameraViewModel.updateTemplateData()
    }

    override fun initView() {

        if (PermissionManager.hasPermissions(this, cameraPermission)) {
            startCamera()
        } else {
            PermissionManager.requestPermissions(requestCameraPermissionLauncher, cameraPermission)
        }
        /* initTemplate(
             TemplateDataModel.getDefaultTemplateData()
         )*/
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
            tvFunction.setOnClickListener {
                if (cameraViewModel.isVideoMode()) {
                    updateCameraMode(false)
                    cameraViewModel.toggleCameraMode()
                }
            }
            tvOption.setOnClickListener {
                if (!cameraViewModel.isVideoMode()) {
                    updateCameraMode(true)
                    cameraViewModel.toggleCameraMode()
                }
            }
            imvTakeCapture.setOnClickListener {
                if (cameraViewModel.isVideoMode()) {
                    toggleVideoRecording()
                } else {
                    takePicture()
                }
            }
            imvFlash.setOnClickListener {
                if (cameraViewModel.currentLensFacing() == CameraSelector.LENS_FACING_FRONT) {
                    Toast.makeText(
                        this@CameraActivity,
                        "Camera trc ko hỗ trợ flash",
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
                    cameraState.previewUri?.let { uri ->
                        //saveVideoToGallery(uri)
                    }
                    cameraState.error?.let {
                        Toast.makeText(this@CameraActivity, it, Toast.LENGTH_SHORT).show()
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
            BitmapHolder.bitmap = imgBitmap
            putExtra("TEMPLATE_DATA", cameraViewModel.cameraState.value.templateData)
            putExtra("TEMPLATE_ID", cameraViewModel.cameraState.value.selectedTemplateId)
            putExtra("FROM_ALBUM", false)
        }
        cameraViewModel.updateCameraState {
            it.copy(
                captureImageBitmap = null
            )
        }
        startActivity(intent)
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
        val templateId = Config.TEMPLATE_4
        cameraViewModel.selectedTemplate(templateId)

        // Tạo template trên màn hình camera
        binding.templateOverlayContainer.addTemplate(
            this,
            templateId,
            template
        )

        // Lưu tham chiếu đến template view
        cameraViewModel.updateCameraState {
            it.copy(
                templateView = binding.templateOverlayContainer
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

    override fun onDestroy() {
        super.onDestroy()
        cameraViewModel.cancelCountDown()
        cameraViewModel.cleanupCamera()
    }
}