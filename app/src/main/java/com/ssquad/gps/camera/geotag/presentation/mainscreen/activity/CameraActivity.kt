package com.ssquad.gps.camera.geotag.presentation.mainscreen.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.TorchState
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.data.models.TemplateDataModel
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.MediaSavedActivity
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.TemplatesActivity
import com.ssquad.gps.camera.geotag.presentation.viewmodel.CameraViewModel
import com.ssquad.gps.camera.geotag.service.MapManager
import com.ssquad.gps.camera.geotag.utils.BitmapHolder
import com.ssquad.gps.camera.geotag.utils.Config
import com.ssquad.gps.camera.geotag.utils.CustomSnackbar
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import com.ssquad.gps.camera.geotag.utils.addTemplate
import com.ssquad.gps.camera.geotag.utils.gone
import com.ssquad.gps.camera.geotag.utils.invisible
import com.ssquad.gps.camera.geotag.utils.loadImageIcon
import com.ssquad.gps.camera.geotag.utils.startCountdownAnimation
import com.ssquad.gps.camera.geotag.utils.visible
import com.google.android.material.snackbar.Snackbar
import com.mapbox.maps.Style
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.databinding.ActivityCameraBinding
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.EditAlbumLibraryActivity
import com.ssquad.gps.camera.geotag.service.MapboxManager
import com.ssquad.gps.camera.geotag.utils.Constants
import com.ssquad.gps.camera.geotag.utils.PermissionManager
import com.ssquad.gps.camera.geotag.utils.setOnDebounceClickListener
import com.ssquad.gps.camera.geotag.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class CameraActivity : BaseActivity<ActivityCameraBinding>(ActivityCameraBinding::inflate) {
    private val templateLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { data ->
                    val selectedTemplateId = data.getStringExtra("SELECTED_TEMPLATE_ID")
                    selectedTemplateId?.let {
                        templateId = it
                        cameraViewModel.updateTemplateData()
                        cameraViewModel.selectedTemplate(it)
                    }
                }
            }
        }
    private var reqNavigate =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        }
    private val cameraViewModel: CameraViewModel by viewModel()

    private var templateId: String? = null
    private var mapSnapshotJob: Job? = null
    private lateinit var mapboxManager:MapboxManager
    private var mapSnapshot: Bitmap? = null
    private var processingSnackbar: Snackbar? = null
    private var successSnackbar: Snackbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startCamera()
        observeTorchState()

    }

    override fun initData() {
        templateId = SharePrefManager.getDefaultTemplate()
        cameraViewModel.getLastCaptureImage()
    }

    override fun initView() {

        initMapBox()
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
            binding.motionLayoutMode.setTransitionListener(object :
                MotionLayout.TransitionListener {
                override fun onTransitionStarted(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int
                ) {
                }

                override fun onTransitionChange(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int,
                    progress: Float
                ) {
                }

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

                override fun onTransitionTrigger(
                    motionLayout: MotionLayout?,
                    triggerId: Int,
                    positive: Boolean,
                    progress: Float
                ) {
                }
            })

            tvFunction.setOnClickListener {
                //CHẾ ĐỘ CHỤP ẢNH
                Log.d("CameraActivity", "tvFunction clicked!")
                if (cameraViewModel.isVideoMode()) {
                    binding.motionLayoutMode.transitionToState(R.id.photo_mode)
                    cameraViewModel.toggleCameraMode()
                    if (cameraViewModel.camera?.cameraInfo?.torchState?.value == TorchState.ON ) {
                        cameraViewModel.toggleFlashDuringRecording()
                        if(cameraViewModel.getCurrentFlashMode() == ImageCapture.FLASH_MODE_OFF){
                            toggleFlashMode()
                            updateFlashIcon(cameraViewModel.getCurrentFlashMode())
                        }
                    }
                }
            }
            tvOption.setOnClickListener {
                //CHẾ ĐỘ QUAY VIDEO
                Log.d("CameraActivity", "tvOption clicked!")
                if (!cameraViewModel.isVideoMode()) {
                    binding.motionLayoutMode.transitionToState(R.id.video_mode)
                    cameraViewModel.toggleCameraMode()
                    if (cameraViewModel.getCurrentFlashMode() == ImageCapture.FLASH_MODE_ON && cameraViewModel.isVideoMode()) {
                        cameraViewModel.toggleFlashDuringRecording()
                    }
                }
            }
            binding.imvTakeCapture.setOnDebounceClickListener(1000L) {
                if (cameraViewModel.cameraState.value.templateData == null) {
                    Toast.makeText(
                        this@CameraActivity,
                        getString(R.string.template_not_loaded_message),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (cameraViewModel.isVideoMode()) {
                        Log.d("CameraActivity", "Video mode")
                        toggleVideoRecording()
                    } else {
                        Log.d("CameraActivity", "Camera mode")
                        takePicture()
                    }
                }
            }
            binding.imvFlash.setOnClickListener {
                if (cameraViewModel.isVideoMode()) {
                    cameraViewModel.toggleFlashDuringRecording()
                } else {
                    toggleFlashMode()
                    updateFlashIcon(cameraViewModel.getCurrentFlashMode())
                }
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

            imvOpenTemplate.setOnClickListener {
                val intent = Intent(this@CameraActivity, TemplatesActivity::class.java)
                templateLauncher.launch(intent)
            }
            imvSelectImage.setOnClickListener {
                if (PermissionManager.checkLibraryGranted(context = this@CameraActivity)) {
                    reqNavigate.launch(
                        Intent(
                            this@CameraActivity,
                            EditAlbumLibraryActivity::class.java
                        )
                    )
                } else {
                    Intent(this@CameraActivity, RequestPermissionActivity::class.java).apply {
                        putExtra(
                            Constants.INTENT_REQUEST_SINGLE_PERMISSION,
                            RequestPermissionActivity.TYPE_GALLERY
                        )
                        putExtra(Constants.INTENT_LIBRARY_PERMISSION, true)
                        reqNavigate.launch(this)
                    }
                }
            }
        }
    }
    private fun initMapBox(){
        mapboxManager = MapboxManager(this)
        mapboxManager.initializeMap(
            mapView = binding.mapboxView,
            mapStyle = Style.SATELLITE_STREETS,
            onMapReady = {
                cameraViewModel.updateTemplateData()
            }
        )
    }
    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cameraViewModel.cameraState.collect { cameraState ->
                    //khi nao` chua có data thi ko cho chup
                    binding.imvTakeCapture.isEnabled = cameraState.templateData != null
                    cameraState.captureImageBitmap?.let { bitmap ->
                        Log.d("CameraActivity", "observeViewModel: $bitmap")
                        navigateToPreviewImage(bitmap)
                    }
                    cameraState.error?.let {
                        Toast.makeText(
                            this@CameraActivity,
                            getString(R.string.error_taking_photo_please_try_again),
                            Toast.LENGTH_SHORT
                        ).show()
                        cameraViewModel.updateCameraState {
                            it.copy(
                                error = null
                            )
                        }
                    }
                    cameraState.recordingDuration?.let { duration ->
                        binding.tvDurationVideo.text = duration
                    }
                    updateRecordingUI(cameraState.isRecording)
                    if (cameraState.isCountDown && cameraState.countDownTimer > 0) {
                        Log.d("CountingDown", "observeViewModel: ${cameraState.countDownTimer}")
                        binding.tvCountDown.text = "${cameraState.countDownTimer}"
                        binding.tvCountDown.visible()
                        binding.imvTakeCapture.isEnabled = false
                        startCountdownAnimation(binding.tvCountDown)
                    } else {
                        binding.imvTakeCapture.isEnabled = true
                        binding.tvCountDown.gone()
                    }
                    if (cameraState.templateData == null) {
                        // Nếu chưa có template thì show shimmer
                        showTemplateLoading(true)
                    } else {
                        // Có template rồi thì ẩn shimmer và initTemplate
                        showTemplateLoading(false)
                        initTemplate(cameraState.templateData)
                    }
                    cameraState.lastCaptureImage?.let { photo ->
                        binding.imvSelectImage.loadImageIcon(photo.path)
                    }
                    if (cameraState.showSuccessSnackbar) {
                        showSuccessSnackbar()
                        cameraViewModel.updateCameraState {
                            it.copy(
                                showSuccessSnackbar = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observeTorchState() {
        val torchState = cameraViewModel.camera?.cameraInfo?.torchState
        if (torchState == null) {
            Log.e("CameraActivity", "NULL ")
            return
        }

        torchState.observe(this) { state ->
            Log.d("CameraActivity", "observeTorchState: $state")
            updateFlashIconDuringRecording(state == TorchState.ON)
        }
    }

    override fun onResume() {
        super.onResume()
        cameraViewModel.getLastCaptureImage()
        startCamera()
        updateFlashIcon(cameraViewModel.getCurrentFlashMode()) // Cập nhật icon flash

    }

    private fun updateFlashIconDuringRecording(isTorchOn: Boolean) {
        val flashIcon = if (isTorchOn) R.drawable.ic_flash_on else R.drawable.ic_flash_off
        binding.imvFlash.setImageResource(flashIcon)
    }

    private fun navigateToPreviewImage(imgBitmap: Bitmap) {
        binding.imvSelectImage.setImageBitmap(imgBitmap)
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

    private fun checkCurrentLensFacing(): Boolean {
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
       try {
           cameraViewModel.toggleVideoRecording(this)
           updateRecordingUI(cameraViewModel.cameraState.value.isRecording)
       }catch (e: Exception){
           showToast(getString(R.string.error_taking_photo_please_try_again))
       }
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
    private fun showTemplateLoading(show: Boolean) {
        binding.skeletonTemplateLoading.apply {
            if (show) {
                maskColor = ContextCompat.getColor(context, R.color.neutralGrey)
                shimmerColor = ContextCompat.getColor(context, R.color.neutralWhite)
                shimmerDurationInMillis = 1000L
                showShimmer = true
                showSkeleton()
                visibility = View.VISIBLE
            } else {
                showOriginal() // ẩn shimmer
                visibility = View.GONE
            }
        }
    }

    private fun initTemplate(template: TemplateDataModel) {

        mapSnapshotJob?.cancel()
        cameraViewModel.selectedTemplate(templateId)
        val isGpsTemplate = Config.isGPSTemplate(templateId)
        if (isGpsTemplate) {
            if (cameraViewModel.cameraState.value.isRecording && mapSnapshot != null) {
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

                mapboxManager.createSnapshot(
                    latitude = lat,
                    longitude = lon,
                    zoom = 14.0,
                    mapStyle = Style.SATELLITE_STREETS,
                    onSnapshotReady = { it ->
                        mapSnapshot = it
                        mapSnapshotJob = lifecycleScope.launch(Dispatchers.Main) {
                            delay(1000)
                            updateTemplateOverlay(template, it)
                            cameraViewModel.updateCameraState {
                                it.copy(templateView = binding.templateOverlayContainer)
                            }
                        }
                    },
                    onError = { error ->
                        Log.e("CameraActivity", "Error creating snapshot: $error")
                        updateTemplateOverlay(template, null)
                    }
                ) 
            } catch (e: Exception) {
                Log.e("CameraActivity", "Error processing location data: ${e.message}")
                updateTemplateOverlay(template, null)
            }
        } else {
            updateTemplateOverlay(template, null)
            cameraViewModel.updateCameraState {
                it.copy(templateView = binding.templateOverlayContainer)
            }
        }
    }

    private fun updateTemplateOverlay(template: TemplateDataModel, bitmap: Bitmap?) {
        if (bitmap != null) {
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
    private fun showSuccessSnackbar() {
        processingSnackbar?.dismiss()
        successSnackbar?.dismiss()

        successSnackbar =
            CustomSnackbar.showSuccessSnackbar(
                view = binding.root,
                message = getString(R.string.success),
            )
        successSnackbar?.show()
    }




    override fun onDestroy() {
        super.onDestroy()
        mapboxManager.destroySnapshotter()
        cameraViewModel.cancelCountDown()
        cameraViewModel.cleanupCamera()
        mapSnapshotJob?.cancel()
        mapSnapshot = null
        processingSnackbar?.dismiss()
        successSnackbar?.dismiss()
    }
}