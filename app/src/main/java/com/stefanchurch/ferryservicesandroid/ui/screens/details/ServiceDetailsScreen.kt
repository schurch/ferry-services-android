package com.stefanchurch.ferryservicesandroid.ui.screens.details

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
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
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.stefanchurch.ferryservicesandroid.R
import com.stefanchurch.ferryservicesandroid.data.model.Service
import com.stefanchurch.ferryservicesandroid.data.model.Service.Location.Weather
import com.stefanchurch.ferryservicesandroid.ui.model.ScheduledDepartureSectionUiModel
import com.stefanchurch.ferryservicesandroid.ui.components.SectionHeading
import com.stefanchurch.ferryservicesandroid.ui.components.ServiceStatusIndicator
import com.stefanchurch.ferryservicesandroid.ui.components.VesselMapMarker
import com.stefanchurch.ferryservicesandroid.ui.components.nextDepartureMapSnippet
import com.stefanchurch.ferryservicesandroid.ui.model.operatorLogoRes
import com.stefanchurch.ferryservicesandroid.util.formatTime
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

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
    var selectedDepartureNote by remember { mutableStateOf<String?>(null) }
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
                            val localDate = pickerMillisToLocalDate(millis)
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

    selectedDepartureNote?.let { note ->
        AlertDialog(
            onDismissRequest = { selectedDepartureNote = null },
            confirmButton = {
                TextButton(onClick = { selectedDepartureNote = null }) {
                    Text("OK")
                }
            },
            title = { Text("Departure note") },
            text = { Text(note) },
        )
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
                        Text(
                            text = service.route,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        DisruptionStatusRow(
                            service = service,
                            onOpenDetails = openWebInfo,
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
                                enabled = !state.loadingSubscribed && state.notificationsAuthorized,
                                onCheckedChange = { viewModel.updateSubscribed(serviceId, it) },
                            )
                        }
                        if (!state.notificationsAuthorized) {
                            Text(
                                "Enable app notifications in Android settings to manage service updates.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                items(service.locations.sortedBy { it.name }) { location ->
                    LocationInfoCard {
                        Text(
                            text = location.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        location.nextDeparture?.let { departure ->
                            LocationInfoItem(
                                icon = {
                                    FerryLineIcon()
                                },
                                label = "Next ferry departure",
                                value = "${formatTime(departure.departure)} to ${departure.destination.name}",
                            )
                        }
                        location.nextRailDeparture?.let { rail ->
                            LocationInfoItem(
                                icon = {
                                    RailLineIcon()
                                },
                                label = "Next rail departure",
                                value = "${formatTime(rail.departure).ifBlank { rail.departure }} to ${rail.to}",
                            )
                        }
                        location.weather?.let { weather ->
                            LocationInfoItem(
                                icon = {
                                    WeatherIcon(
                                        weather = weather,
                                        modifier = Modifier.size(24.dp),
                                    )
                                },
                                label = "Weather",
                                value = "${weather.temperatureCelsius}C, ${weather.description}",
                            )
                            LocationInfoItem(
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.wind),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .rotate(weather.windDirection.toFloat() + 180f),
                                    )
                                },
                                label = "Wind",
                                value = "${weather.windSpeedMph} mph ${weather.windDirectionCardinal}",
                            )
                        } ?: LocationInfoItem(
                            icon = {
                                WeatherIconFallback(modifier = Modifier.size(24.dp))
                            },
                            label = "Weather",
                            value = "Unavailable",
                        )
                    }
                }

                if (state.showSchedule) {
                    item {
                        DetailSection {
                            SectionHeading("Scheduled departures")
                            Button(
                                onClick = {
                                    val millis = localDateToPickerMillis(state.selectedDate)
                                    showDatePicker = millis
                                },
                            ) {
                                Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                                Text(
                                    state.selectedDateLabel,
                                    modifier = Modifier.padding(start = 8.dp),
                                )
                            }
                            val timetableNoteColor = MaterialTheme.colorScheme.onSurfaceVariant
                            if (state.showScheduleWarning) {
                                Text(
                                    "Timetable data may not match live operations. Check the operator for the latest update.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = timetableNoteColor,
                                )
                            }
                            state.sharedDepartureNote?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = timetableNoteColor,
                                )
                            }
                        }
                    }

                    state.scheduleSections.forEachIndexed { index, section ->
                        stickyHeader(
                            key = "departure-header-$index-${section.originName}-${section.destinationName}",
                            contentType = "departure-header",
                        ) {
                            DepartureSectionHeader(section)
                        }

                        item(
                            key = "departure-rows-$index-${section.originName}-${section.destinationName}",
                            contentType = "departure-rows",
                        ) {
                            DetailSection {
                                section.rows.forEach { row ->
                                    val note = row.note?.trim()?.takeIf { it.isNotEmpty() }
                                    val rowColor = if (row.isPastDeparture) {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            row.departureTimeText,
                                            color = rowColor,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Text(
                                            row.arrivalTimeText,
                                            color = rowColor,
                                        )
                                        if (note != null) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(start = 12.dp)
                                                    .size(32.dp)
                                                    .clickable { selectedDepartureNote = note },
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Outlined.Notes,
                                                    contentDescription = "Departure note available",
                                                    tint = rowColor,
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                                section.sharedNote?.let { note ->
                                    Text(
                                        note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    item {
                        TextButton(
                            onClick = {
                                context.startActivity(viewModel.supportEmailIntent(serviceId, state))
                            },
                            modifier = Modifier.wrapContentWidth(Alignment.Start),
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text("Report timetable issue")
                        }
                    }
                }

                item {
                    OperatorContactSection(
                        service = service,
                        onOpenIntent = { intent -> context.startActivity(intent) },
                    )
                }
            }
        }
    }
}

@Composable
private fun OperatorContactSection(
    service: Service,
    onOpenIntent: (Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val operator = service.serviceOperator
    DetailSection(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val logoRes = service.operatorLogoRes()
            if (logoRes != null) {
                Image(
                    painter = painterResource(logoRes),
                    contentDescription = operator?.name,
                    modifier = Modifier.size(28.dp),
                )
            }
            Text(
                text = operator?.name ?: "Unknown operator",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        val phone = operator?.localNumber ?: operator?.internationalNumber
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OperatorContactButton(
                label = "Phone",
                intent = phone?.let { Intent(Intent.ACTION_DIAL, Uri.parse("tel:$it")) },
                onOpenIntent = onOpenIntent,
                modifier = Modifier.weight(1f),
            )
            OperatorContactButton(
                label = "Website",
                intent = operator?.website?.let { Intent(Intent.ACTION_VIEW, Uri.parse(it)) },
                onOpenIntent = onOpenIntent,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OperatorContactButton(
                label = "Email",
                intent = operator?.email?.let { Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$it")) },
                onOpenIntent = onOpenIntent,
                modifier = Modifier.weight(1f),
            )
            OperatorContactButton(
                label = "X",
                intent = operator?.x?.let { Intent(Intent.ACTION_VIEW, Uri.parse(it)) },
                onOpenIntent = onOpenIntent,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OperatorContactButton(
                label = "Facebook",
                intent = operator?.facebook?.let { Intent(Intent.ACTION_VIEW, Uri.parse(it)) },
                onOpenIntent = onOpenIntent,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun DepartureSectionHeader(
    section: ScheduledDepartureSectionUiModel,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = section.originName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = section.destinationName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun OperatorContactButton(
    label: String,
    intent: Intent?,
    onOpenIntent: (Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = { intent?.let(onOpenIntent) },
        enabled = intent != null,
        modifier = modifier,
    ) {
        Text(label)
    }
}

@Composable
private fun LocationInfoItem(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun FerryLineIcon(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(modifier = modifier.size(24.dp)) {
        val stroke = Stroke(
            width = 1.2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val hull = Path().apply {
            moveTo(size.width * 0.18f, size.height * 0.48f)
            lineTo(size.width * 0.82f, size.height * 0.48f)
            lineTo(size.width * 0.68f, size.height * 0.68f)
            lineTo(size.width * 0.30f, size.height * 0.68f)
            close()
        }
        drawPath(hull, color = color, style = stroke)
        drawLine(
            color = color,
            start = Offset(size.width * 0.34f, size.height * 0.35f),
            end = Offset(size.width * 0.70f, size.height * 0.35f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.42f, size.height * 0.24f),
            end = Offset(size.width * 0.62f, size.height * 0.24f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.24f, size.height * 0.78f),
            end = Offset(size.width * 0.76f, size.height * 0.78f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun RailLineIcon(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(modifier = modifier.size(24.dp)) {
        val stroke = Stroke(
            width = 1.2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val train = Path().apply {
            moveTo(size.width * 0.30f, size.height * 0.20f)
            lineTo(size.width * 0.70f, size.height * 0.20f)
            lineTo(size.width * 0.76f, size.height * 0.62f)
            lineTo(size.width * 0.64f, size.height * 0.76f)
            lineTo(size.width * 0.36f, size.height * 0.76f)
            lineTo(size.width * 0.24f, size.height * 0.62f)
            close()
        }
        drawPath(train, color = color, style = stroke)
        drawLine(
            color = color,
            start = Offset(size.width * 0.34f, size.height * 0.38f),
            end = Offset(size.width * 0.66f, size.height * 0.38f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.36f, size.height * 0.86f),
            end = Offset(size.width * 0.64f, size.height * 0.86f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun LocationInfoCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun DisruptionStatusRow(
    service: Service,
    onOpenDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val detailsHtml = service.additionalInfo?.takeIf { it.isNotBlank() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (detailsHtml != null) {
                    Modifier.clickable { onOpenDetails(detailsHtml) }
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ServiceStatusIndicator(service.status)
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
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (detailsHtml != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Show disruption details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(fallbackLocation, 8f)
    }
    val mapStyle = remember(context, darkTheme) {
        if (darkTheme) MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark) else null
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
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.NORMAL,
                mapStyleOptions = mapStyle,
            ),
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
        ) {
            service.locations.forEach { location ->
                Marker(
                    state = MarkerState(LatLng(location.latitude, location.longitude)),
                    title = location.name,
                    snippet = location.nextDepartureMapSnippet(),
                )
            }
            service.vessels.forEach { vessel ->
                VesselMapMarker(vessel)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onOpenMap),
        )
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
            modifier = modifier,
        )
    } else {
        WeatherIconFallback(modifier = modifier)
    }
}

@Composable
private fun WeatherIconFallback(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Outlined.WbSunny,
        contentDescription = null,
        modifier = modifier,
    )
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

private fun localDateToPickerMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun pickerMillisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
