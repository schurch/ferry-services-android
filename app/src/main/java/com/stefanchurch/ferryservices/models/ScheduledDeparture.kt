package com.stefanchurch.ferryservices.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ScheduledDeparture(
    val departure: String,
    val arrival: String,
    val destination: Location
) : Parcelable
