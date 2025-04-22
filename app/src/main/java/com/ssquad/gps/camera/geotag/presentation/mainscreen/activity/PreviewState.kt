package com.ssquad.gps.camera.geotag.presentation.mainscreen.activity

import android.net.Uri
import com.ssquad.gps.camera.geotag.data.models.TemplateState

data class PreviewState (
    val savedImageUri: Uri ? = null,
    val savedVideoUri: Uri ? = null,
    val error: String ? = null,
    val isSaving: Boolean = false,
    val selectedTemplateId: String? = null,
    val templateState: TemplateState = TemplateState()
)
