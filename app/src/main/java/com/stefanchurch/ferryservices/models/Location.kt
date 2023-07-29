package com.stefanchurch.ferryservices.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Location(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val weather: Weather? = null,
    @SerialName("scheduled_departures") val scheduledDepartures: Array<ScheduledDeparture>? = null
) : Parcelable

fun Location.departuresGroupedByDestination(): Array<Array<ScheduledDeparture>> {
    val scheduledDepartures = scheduledDepartures ?: return emptyArray()
    return scheduledDepartures
        .groupBy { it.destination.id }
        .values
        .map { it.toTypedArray() }
        .toTypedArray()
}