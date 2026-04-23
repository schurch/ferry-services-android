package com.stefanchurch.ferryservicesandroid.ui.components

import com.stefanchurch.ferryservicesandroid.data.model.Service
import com.stefanchurch.ferryservicesandroid.util.formatTime

fun Service.Location.nextDepartureMapSnippet(): String? {
    val departure = nextDeparture ?: return null
    val departureTime = formatTime(departure.departure).ifBlank { departure.departure }

    return "Next departure: $departureTime to ${departure.destination.name}"
}
