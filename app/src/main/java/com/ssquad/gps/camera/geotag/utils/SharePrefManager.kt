package com.ssquad.gps.camera.geotag.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.android.gms.maps.GoogleMap
import com.google.gson.Gson
import com.ssquad.gps.camera.geotag.BuildConfig
import com.ssquad.gps.camera.geotag.utils.Constants.CACHED_LOCATION
import com.ssquad.gps.camera.geotag.utils.Constants.CACHED_LOCATION_LAT
import com.ssquad.gps.camera.geotag.utils.Constants.CACHED_LOCATION_LNG

object SharePrefManager {
    private const val SHARE_PREFERENCES_NAME = "${BuildConfig.APPLICATION_ID}.share_preferences"
    private const val TIMER_KEY = "TIMER_KEY"
    private const val TEMPERATURE_KEY = "TEMPERATURE_KEY"
    private const val IS_OPEN_FIRST_APP = "IS_OPEN_FIRST_APP"
    const val KEY_MAP_TYPE = "map_type"
    const val DEFAULT_MAP_TYPE = GoogleMap.MAP_TYPE_NORMAL
    private var sharePreferences: SharedPreferences? = null

    fun initialize(application: Application) {
        sharePreferences = application.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun getPreferences(): SharedPreferences {
        return sharePreferences
            ?: throw IllegalStateException("Chưa khởi tạo SharePrefManager")
    }
    fun isOpenFirstApp(): Boolean {
        return getPreferences().getBoolean(IS_OPEN_FIRST_APP, true)
    }
    fun isFirstOpenApp(isOpen: Boolean) {
        getPreferences().edit() {
            putBoolean(IS_OPEN_FIRST_APP, isOpen)
        }
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
        return getPreferences().getString("DEFAULT_TEMPLATE", Config.TEMPLATE_1)?: Config.TEMPLATE_1
    }

    fun saveMapType(mapType: Int) {
        getPreferences().edit { putInt(KEY_MAP_TYPE, mapType) }
    }

    fun getMapType(): Int {
        return getPreferences().getInt(KEY_MAP_TYPE, DEFAULT_MAP_TYPE)
    }


    fun saveCachedCoordinates(latitude: Double, longitude: Double,location:String) {
        getPreferences().edit {
            putFloat(CACHED_LOCATION_LAT, latitude.toFloat())
            putFloat(CACHED_LOCATION_LNG, longitude.toFloat())
            SharePrefManager.putString(CACHED_LOCATION,location)
        }
    }

    fun getCachedCoordinates(): Triple<Double, Double,String>? {
        val prefs = getPreferences()
        if (!prefs.contains(CACHED_LOCATION_LAT) || !prefs.contains(CACHED_LOCATION_LNG) || !prefs.contains(CACHED_LOCATION)) {
            return null
        }

        val lat = prefs.getFloat(CACHED_LOCATION_LAT, 0f).toDouble()
        val lng = prefs.getFloat(CACHED_LOCATION_LNG, 0f).toDouble()
        val location = prefs.getString(CACHED_LOCATION, null) ?: return null
        return Triple(lat, lng,location)
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
    fun get(key: String, defaultValue: Long): Long {
        return getPreferences().getLong(key, defaultValue)
    }
    fun get(key: String, defaultValue: Int): Int {
        return getPreferences().getInt(key, defaultValue)
    }
    fun put(key: String, value: Long) {
        if (sharePreferences == null) return

        getPreferences().edit {
            putLong(key, value)
        }
    }
    fun put(key: String, value: Int) {
        if (sharePreferences == null) return

        getPreferences().edit {
            putInt(key, value)
        }
    }
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getPreferences().getBoolean(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        getPreferences().edit { putBoolean(key, value) }
    }
    fun putMultipleLongValues(values: Map<String, Long>) {
        getPreferences().edit {
            for ((key, value) in values) {
                putLong(key, value)
            }
        }
    }
    fun clear() {
        getPreferences().edit { clear() }
    }
    fun contains(key: String): Boolean {
        return getPreferences().contains(key) == true
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