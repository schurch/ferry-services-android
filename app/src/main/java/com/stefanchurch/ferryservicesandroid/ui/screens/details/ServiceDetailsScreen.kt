package com.stefanchurch.ferryservicesandroid.ui.screens.details

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.stefanchurch.ferryservicesandroid.R
import com.stefanchurch.ferryservicesandroid.data.model.Service
import com.stefanchurch.ferryservicesandroid.data.model.Service.Location.Weather
import com.stefanchurch.ferryservicesandroid.ui.components.MetadataLine
import com.stefanchurch.ferryservicesandroid.ui.components.SectionHeading
import com.stefanchurch.ferryservicesandroid.ui.components.ServiceStatusIndicator
import com.stefanchurch.ferryservicesandroid.ui.components.statusColor
import com.stefanchurch.ferryservicesandroid.ui.theme.FerryTint
import com.stefanchurch.ferryservicesandroid.util.formatTime
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailsScreen(
    serviceId: Int,
    onBack: () -> Unit,
    openMap: (Int) -> Unit,
    openWebInfo: (String) -> Unit,
    viewModel: ServiceDetailsViewModel = hiltViewModel(),
) {
    val uiState = remember(viewModel, serviceId) { viewModel.uiState(serviceId) }
    val state by uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(serviceId) {
        viewModel.load(serviceId)
    }

    if (state.errorMessage != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            confirmButton = {
                TextButton(onClick = viewModel::dismissError) {
                    Text("OK")
                }
            },
            title = { Text("Error") },
            text = { Text(state.errorMessage.orEmpty()) },
        )
    }

    if (showDatePicker != 0L) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = showDatePicker)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = 0L },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            viewModel.setSelectedDate(serviceId, localDate)
                        }
                        showDatePicker = 0L
                    },
                ) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = 0L }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.navigationTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (state.loading && state.service == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val service = state.service
        if (service == null && state.failedToLoad) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Unable to load this service right now.")
                    Button(onClick = { viewModel.refresh(serviceId) }, modifier = Modifier.padding(top = 12.dp)) {
                        Text("Retry")
                    }
                }
            }
            return@Scaffold
        }

        if (service != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item {
                    InlineServiceMap(
                        service = service,
                        onOpenMap = { openMap(serviceId) },
                    )
                }

                item {
                    DetailSection {
                        Text(
                            text = service.area,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ServiceStatusIndicator(service.status)
                            Text(
                                text = service.route,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 12.dp),
                            )
                        }
                        Text(
                            text = when (service.status) {
                                com.stefanchurch.ferryservicesandroid.data.model.ServiceStatus.NORMAL ->
                                    "There are currently no disruptions with this service"
                                com.stefanchurch.ferryservicesandroid.data.model.ServiceStatus.DISRUPTED ->
                                    "There are disruptions with this service"
                                com.stefanchurch.ferryservicesandroid.data.model.ServiceStatus.CANCELLED ->
                                    "Sailings have been cancelled for this service"
                                com.stefanchurch.ferryservicesandroid.data.model.ServiceStatus.UNKNOWN ->
                                    "There was a problem fetching the service status"
                            },
                            color = statusColor(service.status),
                        )
                    }
                }

                item {
                    DetailSection {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Subscribe to updates", modifier = Modifier.weight(1f))
                            Switch(
                                checked = state.subscribed,
                                enabled = !state.loadingSubscribed && state.registeredForNotifications,
                                onCheckedChange = { viewModel.updateSubscribed(serviceId, it) },
                            )
                        }
                    }
                }

                items(service.locations.sortedBy { it.name }) { location ->
                    DetailSection {
                        SectionHeading(location.name)
                        location.nextDeparture?.let { departure ->
                            MetadataLine(
                                "Next ferry departure",
                                "${formatTime(departure.departure)} to ${departure.destination.name}",
                            )
                            departure.note?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        } ?: MetadataLine("Next ferry departure", "Unavailable")

                        location.nextRailDeparture?.let { rail ->
                            MetadataLine(
                                "Next rail departure",
                                "${rail.departure} ${rail.from} to ${rail.to}",
                            )
                            MetadataLine("Rail info", rail.departureInfo)
                            rail.platform?.let { MetadataLine("Platform", it) }
                        } ?: MetadataLine("Next rail departure", "Unavailable")

                        location.weather?.let { weather ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                WeatherIcon(weather = weather)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        "${weather.temperatureCelsius}C, ${weather.description}",
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.wind),
                                            contentDescription = null,
                                            tint = FerryTint,
                                            modifier = Modifier.rotate(weather.windDirection.toFloat() + 180f),
                                        )
                                        Text("${weather.windSpeedMph} mph ${weather.windDirectionCardinal}")
                                    }
                                }
                            }
                        } ?: MetadataLine("Weather", "Unavailable")
                    }
                }

                if (state.showSchedule) {
                    item {
                        DetailSection {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                SectionHeading("Scheduled departures")
                                OutlinedButton(
                                    onClick = {
                                        val millis = state.selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                        showDatePicker = millis
                                    },
                                ) {
                                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                                    Text(
                                        state.selectedDateLabel,
                                        modifier = Modifier.padding(start = 8.dp),
                                    )
                                }
                            }
                            if (state.showScheduleWarning) {
                                Text(
                                    "Timetable data may not match live operations. Check the operator for the latest update.",
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                            }
                            state.sharedDepartureNote?.let {
                                Text(it, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    items(state.scheduleSections) { section ->
                        DetailSection {
                            Text("${section.originName} to ${section.destinationName}", style = MaterialTheme.typography.titleSmall)
                            section.rows.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        row.departureTimeText,
                                        color = if (row.isPastDeparture) {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                    )
                                    Text(
                                        row.arrivalTimeText,
                                        color = if (row.isPastDeparture) {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                    )
                                }
                                row.note?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            section.sharedNote?.let { note ->
                                Text(note, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                context.startActivity(viewModel.supportEmailIntent(serviceId, state))
                            },
                        ) {
                            Text("Report timetable issue")
                        }
                    }
                }

                service.additionalInfo?.takeIf { it.isNotBlank() }?.let { html ->
                    item {
                        OutlinedButton(onClick = { openWebInfo(html) }) {
                            Text("Disruption information")
                            Icon(Icons.AutoMirrored.Outlined.OpenInNew, null, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }

                item {
                    DetailSection {
                        SectionHeading("Operator")
                        Text(service.serviceOperator?.name ?: "Unknown operator")
                        service.serviceOperator?.website?.let { website ->
                            Text(
                                website,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(website)))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InlineServiceMap(
    service: Service,
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fallbackLocation = remember(service.serviceId) {
        service.locations.firstOrNull()?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(55.640516, -4.823062)
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(fallbackLocation, 8f)
    }
    var mapLoaded by remember(service.serviceId) { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(service.serviceId, mapLoaded) {
        if (!mapLoaded) return@LaunchedEffect
        val points = buildList {
            service.locations.forEach { add(LatLng(it.latitude, it.longitude)) }
            service.vessels.forEach { add(LatLng(it.latitude, it.longitude)) }
        }

        when (points.size) {
            0 -> cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(fallbackLocation, 8f))
            1 -> cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(points.first(), 10f))
            else -> {
                val bounds = LatLngBounds.builder().apply {
                    points.forEach(::include)
                }.build()
                cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 120))
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
    ) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onOpenMap),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapType = MapType.NORMAL),
            uiSettings = MapUiSettings(
                compassEnabled = false,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false,
                zoomGesturesEnabled = false,
                scrollGesturesEnabled = false,
                rotationGesturesEnabled = false,
                tiltGesturesEnabled = false,
            ),
            onMapLoaded = { mapLoaded = true },
            onMapClick = { onOpenMap() },
        ) {
            service.locations.forEach { location ->
                Marker(
                    state = MarkerState(LatLng(location.latitude, location.longitude)),
                    title = location.name,
                )
            }
            service.vessels.forEach { vessel ->
                Marker(
                    state = MarkerState(LatLng(vessel.latitude, vessel.longitude)),
                    title = vessel.name,
                    snippet = vessel.speed?.let { "$it kn" } ?: null,
                )
            }
        }
    }
}

@Composable
private fun WeatherIcon(
    weather: Weather,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val resourceId = remember(weather.icon) {
        context.resources.getIdentifier(
            "ic__${weather.icon.lowercase()}",
            "drawable",
            context.packageName,
        )
    }

    if (resourceId != 0) {
        Icon(
            painter = painterResource(id = resourceId),
            contentDescription = weather.description,
            tint = androidx.compose.ui.graphics.Color.Unspecified,
            modifier = modifier,
        )
    } else {
        Box(
            modifier = modifier
                .background(FerryTint.copy(alpha = 0.12f), CircleShape)
                .padding(10.dp),
        ) {
            Text(
                text = weather.temperatureCelsius.toString(),
                color = FerryTint,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun DetailSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}
