package com.stefanchurch.ferryservicesandroid.ui.screens.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.stefanchurch.ferryservicesandroid.R
import com.stefanchurch.ferryservicesandroid.ui.components.VesselMapMarker
import com.stefanchurch.ferryservicesandroid.ui.components.nextDepartureMapSnippet

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ServiceMapScreen(
    serviceId: Int,
    onBack: () -> Unit,
    viewModel: ServiceMapViewModel = hiltViewModel(),
) {
    val service by viewModel.service.collectAsStateWithLifecycle()
    val fallbackLocation = remember(service?.serviceId) {
        service?.locations?.firstOrNull()?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(55.640516, -4.823062)
    }
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(fallbackLocation, 8f)
    }
    val mapStyle = remember(context, darkTheme) {
        if (darkTheme) MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark) else null
    }
    var mapLoaded by remember { mutableStateOf(false) }
    var cameraReady by remember(service?.serviceId) { mutableStateOf(false) }
    var mapWidthPx by remember { mutableStateOf(0) }
    var mapHeightPx by remember { mutableStateOf(0) }

    LaunchedEffect(serviceId) {
        viewModel.load(serviceId)
    }

    LaunchedEffect(service, mapLoaded, mapWidthPx, mapHeightPx) {
        val currentService = service ?: return@LaunchedEffect
        if (!mapLoaded || mapWidthPx == 0 || mapHeightPx == 0) return@LaunchedEffect

        val locationPoints = currentService.locations.map { LatLng(it.latitude, it.longitude) }
        val points = locationPoints.ifEmpty {
            currentService.vessels.map { LatLng(it.latitude, it.longitude) }
        }

        when (points.size) {
            0 -> cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(fallbackLocation, 8f))
            1 -> cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(points.first(), 10f))
            else -> {
                val bounds = LatLngBounds.builder().apply {
                    points.forEach(::include)
                }.build()
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngBounds(bounds, mapWidthPx, mapHeightPx, 120),
                )
            }
        }
        cameraReady = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(service?.route ?: "Map") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        val currentService = service
        val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()
        if (currentService == null) {
            CircularProgressIndicator(modifier = Modifier.padding(innerPadding).padding(24.dp))
            return@Scaffold
        }

        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .alpha(if (cameraReady) 1f else 0f)
                .onSizeChanged {
                    mapWidthPx = it.width
                    mapHeightPx = it.height
                },
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.NORMAL,
                mapStyleOptions = mapStyle,
            ),
            contentPadding = PaddingValues(bottom = navigationBarPadding.calculateBottomPadding()),
            onMapLoaded = { mapLoaded = true },
        ) {
            currentService.locations.forEach { location ->
                Marker(
                    state = MarkerState(LatLng(location.latitude, location.longitude)),
                    title = location.name,
                    snippet = location.nextDepartureMapSnippet(),
                )
            }
            currentService.vessels.forEach { vessel ->
                VesselMapMarker(vessel, unavailableSpeedLabel = "Speed unknown")
            }
        }
    }
}
