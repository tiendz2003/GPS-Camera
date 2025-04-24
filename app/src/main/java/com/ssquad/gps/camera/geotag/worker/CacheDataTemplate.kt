package com.ssquad.gps.camera.geotag.worker

import android.content.SharedPreferences
import com.ssquad.gps.camera.geotag.data.models.TemplateDataModel
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class CacheDataTemplate {
    private val _templateData = MutableStateFlow<TemplateDataModel?>(null)
    val templateData = _templateData.asStateFlow()
    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "DEFAULT_TEMPLATE") { // Use your actual key name here
            clearCache() // Invalidate cache when temperature unit changes
        }
    }
    init {
        SharePrefManager.getPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
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
    fun clearCache(){
        _templateData.value = null
        lastUpdateTime = 0
    }
    fun cleanup() {
        SharePrefManager.getPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
}