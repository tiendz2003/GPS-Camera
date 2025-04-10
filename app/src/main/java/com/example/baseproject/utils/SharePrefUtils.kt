package com.example.baseproject.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.baseproject.MyApplication
import androidx.core.content.edit

object SharePrefUtils {
    private const val getSharedPreferences = "GPS_CAMERA"
    private const val TIMER_KEY = "TIMER_KEY"
    fun getSharedPreferences(): SharedPreferences {
        return MyApplication.appContext.getSharedPreferences(getSharedPreferences, Context.MODE_PRIVATE)
    }
    fun getTimerPref(): Int {
        return getSharedPreferences().getInt(TIMER_KEY, 0)
    }
    fun setTimerPref(value: Int) {
        getSharedPreferences().edit { putInt(TIMER_KEY, value) }
    }
}