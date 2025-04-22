package com.ssquad.gps.camera.geotag.fragments

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.bases.BaseFragment
import com.ssquad.gps.camera.geotag.data.models.ThemeTemplateModel
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.EditAlbumLibraryActivity
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.MediaSavedActivity
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.PreviewTemplateActivity
import com.ssquad.gps.camera.geotag.databinding.FragmentHomeBinding
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.TemplatesActivity
import com.ssquad.gps.camera.geotag.presentation.hometab.adapter.ThemeTemplateAdapter
import com.ssquad.gps.camera.geotag.utils.Config
import com.ssquad.gps.camera.geotag.utils.PermissionManager
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import com.ssquad.gps.camera.geotag.utils.setupHorizontal
import com.ssquad.gps.camera.geotag.utils.updateSelection


class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private lateinit var adapter: ThemeTemplateAdapter
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private val listTheme = ThemeTemplateModel.getTemplate()

    override fun initData() {
    }

    override fun initView() {


        Log.d("RecyclerView", "List size: ${listTheme.size}")
        adapter = ThemeTemplateAdapter {
            Log.d("RecyclerView", "Binding item: ${it.id}")
            navToPreview(it)
        }
        adapter.submitList(listTheme)
        binding.rcvTheme.setupHorizontal(adapter)
        adapter.updateSelection(SharePrefManager.getDefaultTemplate())
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                // Quyền đã được cấp, gọi hành động
                //onGranted()
            } else {
                Toast.makeText(requireContext(), getString(R.string.warning_camera_location_permission), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun initActionView() {
        binding.viewAllText.setOnClickListener {
            val intent = Intent(requireContext(), TemplatesActivity::class.java)
            startActivity(intent)
        }
        binding.editPhotoCard.setOnClickListener {
            requestMediaPermission {
                val intent = Intent(requireContext(), EditAlbumLibraryActivity::class.java)
                startActivity(intent)
            }
        }
        binding.savedImageCard.setOnClickListener {
           requestMediaPermission {
               startActivity(MediaSavedActivity.getIntent(requireContext(),false))
           }
        }
        binding.savedVideoCard.setOnClickListener {
           requestMediaPermission {
               startActivity(MediaSavedActivity.getIntent(requireContext(), true))
           }
        }
    }

    private fun navToPreview(selectedTemplate: ThemeTemplateModel) {
        val themeType = selectedTemplate.type
        val filterList = ThemeTemplateModel.getTemplate().filter { it.type == themeType } as ArrayList<ThemeTemplateModel>
        val intent = PreviewTemplateActivity.getIntent(requireContext(), selectedTemplate, filterList,themeType)
        startActivity(intent)
    }
    private fun requestMediaPermission(onGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissions = arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )

            if (PermissionManager.hasPermissions(requireContext(), permissions)) {
                onGranted() // Quyền đã được cấp, gọi hành động ngay
                return
            }

            val shouldShowRationale = permissions.any {
                ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), it)
            }

            if (shouldShowRationale) {
                PermissionManager.showPermissionExplanationDialog(requireContext()) {
                    permissionLauncher.launch(permissions)
                }
            } else {
                // Chỉ hiển thị dialog yêu cầu vào Settings nếu quyền chưa được cấp
                PermissionManager.showOpenSettingsDialog(requireContext()) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", requireContext().packageName, null)
                    }
                    startActivity(intent)
                }
            }
        } else {
            onGranted()
        }
    }

    override fun onResume() {
        super.onResume()
        val defaultTemplateId = SharePrefManager.getDefaultTemplate()
        adapter.updateSelection(defaultTemplateId)
        initActionView()
    }
}