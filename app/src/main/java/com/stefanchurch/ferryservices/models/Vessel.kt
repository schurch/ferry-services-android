package com.stefanchurch.ferryservices.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Vessel(
    val mmsi: Int,
    val name: String,
    val speed: Double? = null,
    val course: Double? = null,
    val latitude: Double,
    val longitude: Double,
    @SerialName("last_received") val lastReceived: String
) : Parcelable
