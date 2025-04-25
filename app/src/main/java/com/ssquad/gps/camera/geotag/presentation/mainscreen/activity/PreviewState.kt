package com.ssquad.gps.camera.geotag.presentation.mainscreen.activity

import android.net.Uri
import com.ssquad.gps.camera.geotag.data.models.TemplateDataModel
import com.ssquad.gps.camera.geotag.data.models.TemplateState
import com.ssquad.gps.camera.geotag.worker.CacheDataTemplate

data class PreviewState (
    val savedImageUri: Uri ? = null,
    val savedVideoUri: Uri ? = null,
    val error: String ? = null,
    val isSaving: Boolean = false,
    val selectedTemplateId: String? = null,
    val cacheDataTemplate: TemplateDataModel = TemplateDataModel(),
    val templateState: TemplateState = TemplateState()
)
