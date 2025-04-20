package com.example.baseproject.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.baseproject.MyApplication
import androidx.core.content.edit
import com.example.baseproject.BuildConfig
import com.google.android.gms.maps.GoogleMap
import com.google.gson.Gson

object SharePrefManager {
    private const val SHARE_PREFERENCES_NAME = "${BuildConfig.APPLICATION_ID}.share_preferences"
    private const val TIMER_KEY = "TIMER_KEY"
    private const val TEMPERATURE_KEY = "TEMPERATURE_KEY"
    const val KEY_MAP_TYPE = "map_type"
    const val DEFAULT_MAP_TYPE = GoogleMap.MAP_TYPE_NORMAL
    private var sharePreferences: SharedPreferences? = null

    fun initialize(application: Application) {
        sharePreferences = application.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private fun getPreferences(): SharedPreferences {
        return sharePreferences
            ?: throw IllegalStateException("Chưa khởi tạo SharePrefManager")
    }

    fun getTimerPref(): Int {
        return getPreferences().getInt(TIMER_KEY, 0)
    }

    fun setTimerPref(value: Int) {
        getPreferences().edit { putInt(TIMER_KEY, value) }
    }

    fun setDefaultTemplate(templateId: String) {
        getPreferences().edit { putString("DEFAULT_TEMPLATE", templateId) }
    }

    fun getDefaultTemplate(): String {
        return getPreferences().getString("DEFAULT_TEMPLATE", Config.TEMPLATE_1)?:Config.TEMPLATE_1
    }

    fun saveMapType(mapType: Int) {
        getPreferences().edit { putInt(KEY_MAP_TYPE, mapType) }
    }

    fun getMapType(): Int {
        return getPreferences().getInt(KEY_MAP_TYPE, DEFAULT_MAP_TYPE)
    }

    fun getString(key: String, defaultValue: Any? = null): String? {
        return when (defaultValue) {
            is String -> getPreferences().getString(key, defaultValue)
            else -> getPreferences().getString(key, null)
        }
    }

    fun putString(key: String, value: String) {
        getPreferences().edit { putString(key, value) }
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getPreferences().getBoolean(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        getPreferences().edit { putBoolean(key, value) }
    }

    fun clear() {
        getPreferences().edit { clear() }
    }

    fun getTemperature(): Boolean {
        return getPreferences().getBoolean(TEMPERATURE_KEY, false)
    }

    fun setTemperature(temperature: Boolean) {
        getPreferences().edit { putBoolean(TEMPERATURE_KEY, temperature) }
    }

    inline fun <reified T> get(key: String, defaultObj: T): T {
        return getString(key)?.let {
            Gson().fromJsonWithTypeToken(it)
        } ?: defaultObj
    }

    inline fun <reified T> put(key: String, obj: T) {
        putString(key, Gson().toJsonWithTypeToken(obj))
    }

    inline fun <reified T> put(key: String, obj: List<T>) {
        putString(key, Gson().toJsonWithTypeToken<List<T>>(obj))
    }
}