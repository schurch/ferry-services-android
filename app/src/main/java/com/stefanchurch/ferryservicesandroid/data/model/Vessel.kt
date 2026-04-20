package com.stefanchurch.ferryservicesandroid.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vessel(
    val mmsi: Int,
    val name: String,
    val speed: Double? = null,
    val course: Double? = null,
    val latitude: Double,
    val longitude: Double,
    @SerialName("last_received") val lastReceived: String,
)
