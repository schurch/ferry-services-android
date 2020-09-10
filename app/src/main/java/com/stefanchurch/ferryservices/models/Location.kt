package com.stefanchurch.ferryservices.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Location(
    val name: String,
    val latitude: Double,
    val longitude: Double
) : Parcelable