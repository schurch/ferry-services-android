package com.stefanchurch.ferryservices.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Weather(
    val description: String,
    val icon: String,
    @SerialName("temperature_celsius") val temperatureCelsius: Int,
    @SerialName("wind_speed_mph") val windSpeedMph: Int,
    @SerialName("wind_direction") val windDirection: Int,
    @SerialName("wind_direction_cardinal") val windDirectionCardinal: String
) : Parcelable
