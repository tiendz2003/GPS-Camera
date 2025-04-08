package com.example.baseproject.presentation.mainscreen.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.baseproject.R
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityCameraBinding
import com.example.baseproject.presentation.viewmodel.CameraViewModel
import com.example.baseproject.utils.BitmapHolder
import com.example.baseproject.utils.PermissionManager
import com.example.baseproject.utils.SharePrefUtils
import com.example.baseproject.utils.gone
import com.example.baseproject.utils.invisible
import com.example.baseproject.utils.startCountdownAnimation
import com.example.baseproject.utils.visible
import kotlinx.coroutines.launch

class CameraActivity : BaseActivity<ActivityCameraBinding>(ActivityCameraBinding::inflate) {
    private val cameraViewModel: CameraViewModel by viewModels()
    private val cameraPermission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if(allGranted){
            startCamera()
        }else{
            Toast.makeText(this, "Cần cấp quyền", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun initData() {

    }

    override fun initView() {

        if(PermissionManager.hasPermissions(this,cameraPermission)){
            startCamera()
        }else{
            PermissionManager.requestPermissions(requestCameraPermissionLauncher,cameraPermission)
        }
        val savedTimer = SharePrefUtils.getTimerPref()
        cameraViewModel.updateCameraState {
            it.copy(
                selectedTimerDuration = savedTimer
            )
        }
        updateTimerIcon(savedTimer)
        observeViewModel()
    }

    override fun initActionView() {
        with(binding) {
            imvBack.setOnClickListener {
                finish()
            }
            imvGird.setOnClickListener {
                cameraViewModel.enableGrid()
                gridOverlay.visibility = if (cameraViewModel.isGridEnabled()) View.VISIBLE else View.GONE
            }
            imvTakeCapture.setOnClickListener {
                val selectedTimer = cameraViewModel.cameraState.value.selectedTimerDuration
                if(selectedTimer > 0){
                    takeCountdownPicture(selectedTimer)
                }else{
                    takePicture()
                }
            }
            imvSwitchCamera.setOnClickListener {
                toggleCamera()
            }
            imvFlash.setOnClickListener {
                if (cameraViewModel.currentLensFacing() == CameraSelector.LENS_FACING_FRONT) {
                    Toast.makeText(this@CameraActivity, "Camera trc ko hỗ trợ flash", Toast.LENGTH_SHORT).show()
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
                if(cameraViewModel.getCurrentScreenState()){
                    binding.clRoot.transitionToEnd()
                    binding.imvFullScreen.setImageResource(R.drawable.ic_full_exit)
                    binding.flCamera.elevation = -1f
                    binding.clHeader.elevation = 10f
                    binding.clBottom.elevation = 10f
                }else{
                    binding.clRoot.transitionToStart()
                    binding.imvFullScreen.setImageResource(R.drawable.ic_full)
                    binding.flCamera.elevation = 0f
                    binding.clHeader.elevation = 0f
                    binding.clBottom.elevation = 0f
                }
            }
        }
    }
    fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                cameraViewModel.cameraState.collect{cameraState->
                    cameraState.captureImageBitmap?.let {bitmap->
                        Log.d("CameraActivity", "observeViewModel: $bitmap")
                        navigateToPreviewImage(bitmap)
                    }
                    cameraState.previewUri?.let {uri->
                        navigateToPreviewVideo(uri)
                    }
                    cameraState.error?.let {
                        Toast.makeText(this@CameraActivity, it, Toast.LENGTH_SHORT).show()
                    }
                    if (cameraState.countDownTimer > 0) {
                        binding.tvCountDown.visible()
                        Log.d("CameraActivity", "observeViewModel: ${cameraState.countDownTimer}")
                        binding.tvCountDown.text = cameraState.countDownTimer.toString()
                        //startCountdownAnimation(binding.tvCountDown)
                    } else {
                        Log.d("CameraActivity", "gone")
                        binding.tvCountDown.gone()
                    }


                }
            }
        }
    }
    private fun navigateToPreviewImage(imgBitmap: Bitmap) {
        val intent = Intent(this, PreviewActivity::class.java).apply {
            putExtra("IS_IMAGE", true)
            BitmapHolder.bitmap = imgBitmap
            cameraViewModel.updateCameraState {
                it.copy(
                    captureImageBitmap = null
                )
            }
        }
        startActivity(intent)
    }
    fun navigateToPreviewVideo(videoUri: Uri) {
        val intent = Intent(this, PreviewActivity::class.java).apply {
            putExtra("IS_IMAGE", false)
            putExtra("VIDEO_URI", videoUri.toString())
        }
        startActivity(intent)
    }
    fun startCamera() {
        cameraViewModel.initializeCamera(this,binding.previewView,this)
    }
    fun takePicture() {
        cameraViewModel.takePhoto()
    }
    fun takeCountdownPicture(timerSeconds: Int){
        cameraViewModel.takePhoto(timerSeconds)
    }
    fun toggleCamera() {
        cameraViewModel.toggleCamera(binding.previewView,this)
    }
    fun toggleFlashMode() {
        cameraViewModel.toggleFlashMode(binding.previewView,this)
    }
    fun toggleVideoRecording() {
        cameraViewModel.toggleVideoRecording(this)
    }
    private fun updateFlashIcon(flashMode:Int){
        val flashIcon =when(flashMode){
            ImageCapture.FLASH_MODE_OFF -> R.drawable.ic_flash_off
            ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
            else -> R.drawable.ic_flash_off
        }
        binding.imvFlash.setImageResource(flashIcon)
    }
    private fun setCountDownTimer() {
        val currentTimerValue = cameraViewModel.cameraState.value.selectedTimerDuration
        val newTimerValue = when(currentTimerValue) {
            0 -> 3
            3 -> 5
            5 -> 10
            else -> 0
        }

        SharePrefUtils.setTimerPref(newTimerValue)
        cameraViewModel.updateCameraState {
            it.copy(
                selectedTimerDuration = newTimerValue
            )
        }
        updateTimerIcon(newTimerValue)

    }
    private fun updateTimerIcon(timerValue: Int) {
        val iconRes = when(timerValue) {
            0 -> R.drawable.ic_time
            3 -> R.drawable.ic_time_3s
            5 -> R.drawable.ic_time_5s
            10 -> R.drawable.ic_time_10s
            else -> R.drawable.ic_time
        }
        binding.imvTimer.setImageResource(iconRes)
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraViewModel.cleanupCamera()
    }
}