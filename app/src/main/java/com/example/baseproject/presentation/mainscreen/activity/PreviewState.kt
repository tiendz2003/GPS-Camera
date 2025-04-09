package com.example.baseproject.presentation.mainscreen.activity

import android.net.Uri

data class PreviewState (
    val savedImageUri: Uri ? = null,
    val savedVideoUri: Uri ? = null,
    val error: String ? = null,
    val isSaving: Boolean = false,
){
}