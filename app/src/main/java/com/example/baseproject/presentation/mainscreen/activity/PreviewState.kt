package com.example.baseproject.presentation.mainscreen.activity

import android.net.Uri
import com.example.baseproject.data.models.TemplateState

data class PreviewState (
    val savedImageUri: Uri ? = null,
    val savedVideoUri: Uri ? = null,
    val error: String ? = null,
    val isSaving: Boolean = false,
    val templateState: TemplateState = TemplateState()
)
