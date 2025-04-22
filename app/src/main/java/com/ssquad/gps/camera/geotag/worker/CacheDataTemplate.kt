package com.ssquad.gps.camera.geotag.worker

import com.ssquad.gps.camera.geotag.data.models.TemplateDataModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class CacheDataTemplate {
    private val _templateData = MutableStateFlow<TemplateDataModel?>(null)
    val templateData = _templateData.asStateFlow()

    private var lastUpdateTime:Long = 0
    private val cacheDuration = 15 * 60 * 1000

    fun updateData(dataModel: TemplateDataModel){
        _templateData.value = dataModel
        lastUpdateTime = System.currentTimeMillis()
    }
    //check thoi gian con hop le khong
    fun isCacheValid(): Boolean {
        return _templateData.value != null && (System.currentTimeMillis() - lastUpdateTime) < cacheDuration
    }
}