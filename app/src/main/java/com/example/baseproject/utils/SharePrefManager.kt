package com.example.baseproject.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.baseproject.MyApplication
import androidx.core.content.edit
import com.google.android.gms.maps.GoogleMap

object SharePrefManager {
    private const val getSharedPreferences = "GPS_CAMERA"
    private const val TIMER_KEY = "TIMER_KEY"
    private const val TEMPERATURE_KEY = "TEMPERATURE_KEY"
    const val KEY_MAP_TYPE = "map_type"
    const val DEFAULT_MAP_TYPE = GoogleMap.MAP_TYPE_NORMAL
    fun getSharedPreferences(): SharedPreferences {
        return MyApplication.appContext.getSharedPreferences(getSharedPreferences, Context.MODE_PRIVATE)
    }
    fun getTimerPref(): Int {
        return getSharedPreferences().getInt(TIMER_KEY, 0)
    }
    fun setTimerPref(value: Int) {
        getSharedPreferences().edit { putInt(TIMER_KEY, value) }
    }
    fun setDefaultTemplate(templateId: String) {
        getSharedPreferences().edit { putString("DEFAULT_TEMPLATE", templateId) }
    }
    fun getDefaultTemplate(): String? {
        return getSharedPreferences().getString("DEFAULT_TEMPLATE", Config.TEMPLATE_1)
    }
    fun saveMapType(mapType: Int) {
        getSharedPreferences().edit { putInt(KEY_MAP_TYPE, mapType) }
    }
    fun getMapType(): Int {
        return getSharedPreferences().getInt(KEY_MAP_TYPE, DEFAULT_MAP_TYPE)
    }
    fun getString(key: String, defaultValue: String): String {
        return getSharedPreferences().getString(key, defaultValue) ?: defaultValue
    }

    fun putString(key: String, value: String) {
        getSharedPreferences().edit() { putString(key, value) }
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getSharedPreferences().getBoolean(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        getSharedPreferences().edit() { putBoolean(key, value) }
    }

    fun clear() {
        getSharedPreferences().edit() { clear() }
    }
    fun getTemperature(): Boolean {
        return getSharedPreferences().getBoolean(TEMPERATURE_KEY, false)
    }

    fun setTemperature(temperature: Boolean) {
        getSharedPreferences().edit { putBoolean(TEMPERATURE_KEY, temperature) }
    }
}