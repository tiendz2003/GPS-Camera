package com.example.baseproject.presentation.mainscreen.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.baseproject.R
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityCameraBinding
import com.example.baseproject.presentation.viewmodel.CameraViewModel
import com.example.baseproject.utils.PermissionManager
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
    }

    override fun initActionView() {

    }
    fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                cameraViewModel.cameraState.collect{cameraState->
                    cameraState.captureImageBitmap?.let {
                      //  navigateToPreview(it)
                    }
                    cameraState.previewUri?.let {
                        //binding.previewView.setImageBitmap(it)
                    }
                    cameraState.error?.let {
                        Toast.makeText(this@CameraActivity, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    fun startCamera() {
        cameraViewModel.initializeCamera(this,binding.previewView,this)
    }
    fun takePicture() {
        cameraViewModel.takePhoto()
    }
    fun takeCountdownPicture(timerSeconds: Int?){
        cameraViewModel.takePhoto(timerSeconds?:0)
    }
    fun toggleCamera() {
        cameraViewModel.toggleCamera(this,binding.previewView,this)
    }
    fun toggleFlashMode() {
        cameraViewModel.toggleFlashMode(this,binding.previewView,this)
    }
    fun toggleVideoRecording() {
        cameraViewModel.toggleVideoRecording(this)
    }
}