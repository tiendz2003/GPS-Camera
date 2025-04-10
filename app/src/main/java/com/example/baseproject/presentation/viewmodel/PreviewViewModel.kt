package com.example.baseproject.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject.domain.CameraRepository
import com.example.baseproject.presentation.mainscreen.activity.PreviewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PreviewViewModel(
    private val cameraRepository: CameraRepository,
) : ViewModel() {
    private val _previewUiState = MutableStateFlow<PreviewState>(PreviewState())
    val previewUiState = _previewUiState.asStateFlow()

    fun updatePreviewState(update: (PreviewState) -> PreviewState) {
        return _previewUiState.update(update)
    }

    fun saveImageToGallery(context: Context, bitmap: Bitmap) {
        viewModelScope.launch {
            updatePreviewState {
                it.copy(
                    isSaving = true
                )
            }
            try {
                val uri = cameraRepository.saveImageToGallery(context, bitmap)
                updatePreviewState {
                    it.copy(
                        savedImageUri = uri,
                    )
                }
            } catch (e: Exception) {
                updatePreviewState {
                    it.copy(
                        error = e.message,
                    )
                }
            }finally {
                updatePreviewState {
                    it.copy(
                        isSaving = false
                    )
                }
            }
        }
    }
    fun saveVideoToGallery(context: Context, uri: Uri) {
        viewModelScope.launch {
            updatePreviewState {
                it.copy(
                    isSaving = true
                )
            }
            try {
                val uri = cameraRepository.saveVideoToGallery(context, uri)
                updatePreviewState {
                    it.copy(
                        savedVideoUri = uri
                    )
                }
            }catch (e: Exception){
                updatePreviewState {
                    it.copy(
                        error = e.message
                    )
                }
            }finally {
                updatePreviewState {
                    it.copy(
                        isSaving = false
                    )
                }
            }
        }
    }
}