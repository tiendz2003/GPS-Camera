package com.ssquad.gps.camera.geotag.presentation.mainscreen.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.utils.GoogleENative
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.presentation.mainscreen.bases.BaseFragment
import com.ssquad.gps.camera.geotag.data.models.ThemeTemplateModel
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.EditAlbumLibraryActivity
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.MediaSavedActivity
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.PreviewTemplateActivity
import com.ssquad.gps.camera.geotag.databinding.FragmentHomeBinding
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.TemplatesActivity
import com.ssquad.gps.camera.geotag.presentation.hometab.adapter.ThemeTemplateAdapter
import com.ssquad.gps.camera.geotag.presentation.mainscreen.activity.RequestPermissionActivity
import com.ssquad.gps.camera.geotag.utils.AdsManager
import com.ssquad.gps.camera.geotag.utils.Constants
import com.ssquad.gps.camera.geotag.utils.PermissionManager
import com.ssquad.gps.camera.geotag.utils.RemoteConfig
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import com.ssquad.gps.camera.geotag.utils.gone
import com.ssquad.gps.camera.geotag.utils.scrollToCenter
import com.ssquad.gps.camera.geotag.utils.setupHorizontal
import com.ssquad.gps.camera.geotag.utils.updateSelection
import com.ssquad.gps.camera.geotag.utils.visible


class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private lateinit var adapter: ThemeTemplateAdapter
    private var lastDefaultTemplateId: String? = null // Biến lưu giá trị trước đó
    private val listTheme = ThemeTemplateModel.getTemplate()
    private var reqNavigate = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedTemplateId = result.data?.getStringExtra("SELECTED_TEMPLATE_ID")
            if (selectedTemplateId != null) {
                listTheme.forEach { it.isSelected = it.id == selectedTemplateId }
                adapter.updateSelection(selectedTemplateId)
            }
        }
    }
    override fun initData() {
    }

    override fun initView() {

        setupRecycleView()
        Log.d("RecyclerView", "List size: ${listTheme.size}")

    }

    override fun initActionView() {
        binding.viewAllText.setOnClickListener {
            val intent = Intent(requireContext(), TemplatesActivity::class.java)
            startActivity(intent)
        }
        binding.editPhotoCard.setOnClickListener {
            if (PermissionManager.checkLibraryGranted(requireContext()) && PermissionManager.checkLocationGranted(requireContext())) {
                reqNavigate.launch(Intent(requireContext(), EditAlbumLibraryActivity::class.java))
            } else {
                Intent(requireContext(), RequestPermissionActivity::class.java).apply {
                    putExtra(Constants.INTENT_REQUEST_SINGLE_PERMISSION, RequestPermissionActivity.TYPE_GALLERY)
                    putExtra(Constants.INTENT_LIBRARY_PERMISSION, true)
                    reqNavigate.launch(this)
                }
            }
        }
        binding.savedImageCard.setOnClickListener {
            if (PermissionManager.checkLibraryGranted(requireContext())) {
                reqNavigate.launch(MediaSavedActivity.getIntent(requireContext(), false))
            } else {
                Intent(requireContext(), RequestPermissionActivity::class.java).apply {
                    putExtra(Constants.INTENT_REQUEST_SINGLE_PERMISSION, RequestPermissionActivity.TYPE_GALLERY)
                    reqNavigate.launch(this)
                }
            }
        }
        binding.savedVideoCard.setOnClickListener {
            if (PermissionManager.checkLibraryGranted(requireContext())) {
                reqNavigate.launch(MediaSavedActivity.getIntent(requireContext(), true))
            } else {
                Intent(requireContext(), RequestPermissionActivity::class.java).apply {
                    putExtra(Constants.INTENT_REQUEST_SINGLE_PERMISSION, RequestPermissionActivity.TYPE_GALLERY)
                    reqNavigate.launch(this)
                }
            }
        }
    }
    private fun setupRecycleView(){
        adapter = ThemeTemplateAdapter {
            Log.d("RecyclerView", "Binding item: ${it.id}")
            navToPreview(it)
        }
        binding.rcvTheme.setupHorizontal(adapter)
        binding.rcvTheme.setHasFixedSize(true)
        adapter.submitList(listTheme){

        }
    }
    private fun navToPreview(selectedTemplate: ThemeTemplateModel) {
        val themeType = selectedTemplate.type
        val filterList = listTheme.filter { it.type == themeType } as ArrayList<ThemeTemplateModel>
        val intent = PreviewTemplateActivity.getIntent(requireContext(), selectedTemplate, filterList, themeType)
        reqNavigate.launch(intent) // Sử dụng ActivityResultLauncher để nhận kết quả
    }

    private fun initNativeAd() {
        when (RemoteConfig.remoteNativeHome) {
            0L -> {
                binding.nativeAdHomeContainer.gone()
            }

            1L -> {
                binding.nativeAdHomeContainer.visible()

                AdmobLib.loadAndShowNative(
                    activity = requireActivity(),
                    admobNativeModel = AdsManager.admobNativeHome,
                    layout = R.layout.custom_ads_native_as_button,
                    size = GoogleENative.UNIFIED_MEDIUM_LIKE_BUTTON,
                    viewGroup = binding.nativeAdHomeContainer,
                )
            }
        }
    }
    override fun onResume() {
        super.onResume()
        initNativeAd()
        val defaultTemplateId = SharePrefManager.getDefaultTemplate()
        // Kiểm tra nếu defaultTemplateId thay đổi
        if (defaultTemplateId != lastDefaultTemplateId) {
            lastDefaultTemplateId = defaultTemplateId // Cập nhật giá trị mới
            adapter.updateSelection(defaultTemplateId)

            // Tìm vị trí của template đã chọn
            val selectedPosition = listTheme.indexOfFirst { it.id == defaultTemplateId }
            if (selectedPosition != -1) {
                binding.rcvTheme.post {
                    binding.rcvTheme.scrollToCenter(selectedPosition)
                }
            }
        }
        initActionView()
    }
}