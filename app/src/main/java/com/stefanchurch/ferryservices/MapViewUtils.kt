package com.stefanchurch.ferryservices

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.stefanchurch.ferryservices.models.Vessel
import kotlin.math.cos
import kotlin.math.sin

/**
 * Remembers a MapView and gives it the lifecycle of the current LifecycleOwner
 */
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        // Make MapView follow the current lifecycle
        val lifecycleObserver = getMapLifecycleObserver(mapView)
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

private fun getMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
            Lifecycle.Event.ON_START -> mapView.onStart()
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            Lifecycle.Event.ON_STOP -> mapView.onStop()
            Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
            else -> throw IllegalStateException()
        }
    }

fun convertVesselToMarkerOptions(vessel: Vessel) : MarkerOptions {
    val rotation = vessel.course ?: 0.0
    val x = sin(-rotation * Math.PI / 180) * 0.5 + 0.5
    val y = -(cos(-rotation * Math.PI / 180) * 0.5 - 0.5)

    return MarkerOptions()
        .title(vessel.name)
        .snippet(vessel.speed?.let{ "$it knots" })
        .position(LatLng(vessel.latitude, vessel.longitude))
        .anchor(0.5f, 0.5f)
        .rotation(rotation.toFloat())
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ferry))
        .flat(true)
        .infoWindowAnchor(x.toFloat(), y.toFloat())
        .zIndex(1.0f)
}