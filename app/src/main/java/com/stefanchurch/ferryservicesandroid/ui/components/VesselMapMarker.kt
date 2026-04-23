package com.stefanchurch.ferryservicesandroid.ui.components

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.stefanchurch.ferryservicesandroid.R
import com.stefanchurch.ferryservicesandroid.data.model.Vessel
import com.stefanchurch.ferryservicesandroid.util.parseInstant
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VesselMapMarker(
    vessel: Vessel,
    unavailableSpeedLabel: String? = null,
) {
    val icon = remember { BitmapDescriptorFactory.fromResource(R.drawable.ferry) }
    val rotation = vessel.course ?: 0.0
    val markerState = rememberUpdatedMarkerState(LatLng(vessel.latitude, vessel.longitude))

    Marker(
        state = markerState,
        title = vessel.name,
        snippet = vessel.mapSnippet(unavailableSpeedLabel),
        icon = icon,
        anchor = Offset(0.5f, 0.5f),
        flat = true,
        rotation = rotation.toFloat(),
        infoWindowAnchor = vesselInfoWindowAnchor(rotation),
        zIndex = 1f,
        onClick = {
            markerState.showInfoWindow()
            true
        },
    )
}

private fun vesselInfoWindowAnchor(rotation: Double): Offset {
    val radians = -rotation * Math.PI / 180
    val x = sin(radians) * 0.5 + 0.5
    val y = -(cos(radians) * 0.5 - 0.5)

    return Offset(x.toFloat(), y.toFloat())
}

private fun Vessel.mapSnippet(unavailableSpeedLabel: String?): String? {
    val updated = lastReceived.relativeUpdateText()
    val speedText = speed?.let { "${it.knotsText()} knots" }

    return when {
        speedText != null && updated != null -> "$speedText • $updated"
        speedText != null -> speedText
        updated != null -> "Updated $updated"
        else -> unavailableSpeedLabel
    }
}

private fun String.relativeUpdateText(): String? {
    val instant = parseInstant(this) ?: return null

    return DateUtils.getRelativeTimeSpanString(
        instant.toEpochMilli(),
        System.currentTimeMillis(),
        0L,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()
}

private fun Double.knotsText(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        toString()
    }
}
