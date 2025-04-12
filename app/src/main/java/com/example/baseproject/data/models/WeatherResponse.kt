package com.example.baseproject.data.models

import com.google.gson.annotations.SerializedName

data class WttrResponse(
    @SerializedName("current_condition") val currentCondition: List<CurrentCondition>
)

data class CurrentCondition(
    @SerializedName("temp_C") val tempC: String,
    @SerializedName("temp_F") val tempF: String,
    @SerializedName("weatherDesc") val weatherDesc: List<WeatherDesc>,
    @SerializedName("humidity") val humidity: String
)

data class WeatherDesc(
    @SerializedName("value") val value: String
)