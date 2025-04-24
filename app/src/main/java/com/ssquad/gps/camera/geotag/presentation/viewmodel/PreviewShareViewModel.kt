package com.ssquad.gps.camera.geotag.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssquad.gps.camera.geotag.data.models.TemplateState
import com.ssquad.gps.camera.geotag.domain.CameraRepository
import com.ssquad.gps.camera.geotag.presentation.mainscreen.activity.PreviewState
import com.ssquad.gps.camera.geotag.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PreviewShareViewModel(
    private val cameraRepository: CameraRepository,
) : ViewModel() {
    private val _previewUiState = MutableStateFlow(PreviewState())
    val previewUiState = _previewUiState.asStateFlow()

    private val _toastEvents = Channel<String>(Channel.BUFFERED)
    val toastEvents = _toastEvents.receiveAsFlow()

    private val _navigationEvents = Channel<Unit>(Channel.BUFFERED)
    val navigationEvents = _navigationEvents.receiveAsFlow()

    fun updatePreviewState(update: (PreviewState) -> PreviewState) {
        return _previewUiState.update(update)
    }
    fun updateTemplateState(newState: TemplateState){
        updatePreviewState {
            it.copy(
                templateState = newState
            )
        }
    }
    fun setSelectedTemplate(templateId: String) {
        updatePreviewState {
            it.copy(
                selectedTemplateId = templateId
            )
        }
    }
    fun resetCustomTemplate() {
        updatePreviewState {
            it.copy(
                templateState = TemplateState()
            )
        }
    }
    fun saveImageToGallery(context: Context, bitmap: Bitmap) {
        viewModelScope.launch {
            updatePreviewState {
                it.copy(
                    isSaving = true
                )
            }
            _toastEvents.send(context.getString(R.string.saving_image))
            try {
                val uri = cameraRepository.saveImageToGallery(context, bitmap)
                updatePreviewState {
                    it.copy(
                        savedImageUri = uri,
                    )
                }
                _toastEvents.send(context.getString(R.string.image_saved_successfully))
                _navigationEvents.send(Unit)
            } catch (e: Exception) {
                updatePreviewState {
                    it.copy(
                        error = e.message,
                    )
                }
                _toastEvents.send(context.getString(R.string.save_image_failed))
                _navigationEvents.send(Unit)
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