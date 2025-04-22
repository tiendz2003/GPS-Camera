package com.ssquad.gps.camera.geotag.presentation.settingtab.activity

import android.location.Location

data class MapSettingState(
    val currentAddress:String? = null,
    val currentLocation: Location? = null,
    val yourLocation: Location? = null,
    val isLoading: Boolean = false,
    val isError: String? = null,
)