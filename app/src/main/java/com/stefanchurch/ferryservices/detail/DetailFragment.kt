package com.stefanchurch.ferryservices.detail

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider.getUriForFile
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.stefanchurch.ferryservices.databinding.DetailFragmentBinding
import com.stefanchurch.ferryservices.models.Status
import com.stefanchurch.ferryservices.models.status
import com.google.maps.android.ktx.awaitMap
import com.stefanchurch.ferryservices.*
import com.stefanchurch.ferryservices.R
import com.stefanchurch.ferryservices.models.Location
import com.stefanchurch.ferryservices.models.Service
import com.stefanchurch.ferryservices.models.Vessel
import com.stefanchurch.ferryservices.models.Weather
import com.stefanchurch.ferryservices.models.departuresGroupedByDestination
import io.sentry.Sentry
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min
import android.text.format.DateFormat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeSlotReusePolicy
import com.stefanchurch.ferryservices.models.ScheduledDeparture
import java.time.Instant

class DetailFragment : Fragment() {

    private val args: DetailFragmentArgs by navArgs()

    private val viewModel: DetailViewModel by viewModels {
        DetailViewModelFactory(
            args.serviceDetailArgument,
            ServicesRepository.getInstance(requireContext().applicationContext),
            SharedPreferences(requireContext().applicationContext),
            this
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<DetailFragmentBinding>(
            inflater,
            R.layout.detail_fragment,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.detailScreen.setContent {
            FerriesTheme {
                DetailScreen(viewModel = viewModel)
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        viewModel.refresh()
    }

    @Composable
    private fun DetailScreen(viewModel: DetailViewModel) {
        viewModel.service.value?.let { service ->
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Box(
                    Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                ) {
                    LocationsMapView(
                        locations = service.locations,
                        vessels = viewModel.vessels.value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clickable {
                                val direction = DetailFragmentDirections.actionDetailFragmentToMap(
                                    service = service,
                                    title = service.route
                                )
                                navigate(direction)
                            }
                    )
                }
                Column(modifier = Modifier.padding(all = 20.dp)) {
                    DetailBody(service = service)
                }
            }
        } ?: run {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Loading...",
                    color = MaterialTheme.colors.secondary,
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }

    @Composable
    private fun DetailBody(service: Service) {
        Text(
            text = service.area,
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.h5
        )
        Text(
            text = service.route,
            color = MaterialTheme.colors.secondary,
            style = MaterialTheme.typography.h6
        )

        Spacer(modifier = Modifier.height(10.dp))

        DisruptionInfoRow(service = service)

        Spacer(modifier = Modifier.height(10.dp))

        SubscribeToUpdatesRow()

        Spacer(modifier = Modifier.height(10.dp))

        TimetableButton(
            title = "VIEW SUMMER 2023 TIMETABLE",
            path = "Timetables/2023/Summer",
            serviceID = service.serviceID
        )

        Spacer(modifier = Modifier.height(10.dp))

        service.locations.mapNotNull { Location(location = it)}
    }

    @Composable
    private fun DisruptionInfoRow(service: Service) {
        val hasAdditionalInfo = ((service.additionalInfo?.length) ?: 0) > 0
        val modifier = if (hasAdditionalInfo)
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = true),
                    onClick = {
                        navigate(
                            DetailFragmentDirections.actionDetailFragmentToAdditional(
                                service
                            )
                        )
                    }
                )
        else Modifier.fillMaxWidth()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
        ) {
            val color = when (service.status) {
                Status.NORMAL -> colorResource(id = R.color.colorStatusNormal)
                Status.DISRUPTED -> colorResource(id = R.color.colorStatusDisrupted)
                Status.CANCELLED -> colorResource(id = R.color.colorStatusCancelled)
                Status.UNKNOWN -> colorResource(id = R.color.colorStatusUnknown)
            }
            Canvas(modifier = Modifier.size(25.dp), onDraw = {
                drawCircle(color = color)
            })
            Spacer(modifier = Modifier.width(15.dp))
            val text = when (service.status) {
                Status.NORMAL -> "There are currently no disruptions with this service"
                Status.DISRUPTED -> "There are disruptions with this service"
                Status.CANCELLED -> "Sailings have been cancelled for this service"
                Status.UNKNOWN -> "Unable to fetch the disruption status for this service"
            }
            Text(
                text = text,
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (hasAdditionalInfo) {
                Spacer(modifier = Modifier.width(15.dp))
                Icon(
                    imageVector = Icons.Outlined.Info,
                    tint = MaterialTheme.colors.secondary,
                    contentDescription = "Additional Info"
                )
            }
        }
    }

    @Composable
    private fun SubscribeToUpdatesRow() {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Subscribe to updates",
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.body1
            )
            Switch(
                checked = viewModel.isSubscribed.value,
                onCheckedChange = { newValue ->
                    viewModel.updatedSubscribedStatus(newValue)
                },
                enabled = viewModel.subscribedEnabled.value,
                colors = SwitchDefaults.colors(checkedThumbColor = colorResource(id = R.color.colorAccent))
            )
        }
    }

    @Composable
    private fun Location(location: Location) {
        Divider(color = MaterialTheme.colors.secondaryVariant, thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = location.name,
            color = MaterialTheme.colors.secondary,
            style = MaterialTheme.typography.h5
        )

        location.weather?.let { weather ->
            Spacer(modifier = Modifier.height(10.dp))
            Weather(weather)
        }

        Spacer(modifier = Modifier.height(20.dp))

        location.departuresGroupedByDestination().map { scheduledDepartures ->
            DepartureHeader(
                fromLocationName = location.name,
                toLocationName = scheduledDepartures.first().destination.name
            )

            Spacer(modifier = Modifier.height(4.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                scheduledDepartures.map { scheduledDeparture ->
                    DepartureRow(departure = scheduledDeparture)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(2.dp))
    }

    @Composable
    private fun DepartureHeader(fromLocationName: String, toLocationName: String) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = fromLocationName,
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.h6
            )
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "to",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colors.secondary
            )
            Text(
                text = toLocationName,
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.h6
            )
        }
    }

    @Composable
    private fun DepartureRow(departure: ScheduledDeparture) {
        fun formatTime(time: Instant): String {
            val formatter = when (DateFormat.is24HourFormat(context)) {
                true -> DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
                false -> DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            }

            return time
                .atZone(ZoneId.of("Europe/London"))
                .format(formatter)
        }

        val color = when (departure.departure > Instant.now()) {
            true -> MaterialTheme.colors.secondary
            false -> MaterialTheme.colors.dullText
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = formatTime(departure.departure),
                color = color,
                style = MaterialTheme.typography.body1
            )
            Text(
                text = formatTime(departure.arrival),
                color = color,
                style = MaterialTheme.typography.body1
            )
        }
    }

    @Composable
    private fun Weather(weather: Weather) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val iconResourceId = expressionOrNull {
                        resources.getIdentifier(
                            "@drawable/ic__${weather.icon.lowercase()}",
                            null,
                            requireContext().packageName
                        )
                    }

                    iconResourceId?.let { resourceId ->
                        if (resourceId != 0) {
                            Image(
                                painter = painterResource(id = resourceId),
                                contentDescription = weather.description,
                                contentScale = ContentScale.None,
                                modifier = Modifier
                                    .height(40.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Sentry.captureMessage("Missing resource ID for weather icon: ${weather.icon.lowercase()}")
                        }
                    } ?: run {
                        Sentry.captureMessage("Exception getting resource ID for weather icon: ${weather.icon.lowercase()}")
                    }

                    Text(
                        text = "${weather.temperatureCelsius}ºC",
                        color = MaterialTheme.colors.primary,
                        style = MaterialTheme.typography.h6
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = weather.description,
                    color = MaterialTheme.colors.secondary,
                    style = MaterialTheme.typography.body1
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.wind),
                        contentDescription = "Wind direction arrow",
                        contentScale = ContentScale.None,
                        modifier = Modifier
                            .rotate(weather.windDirection.toFloat() + 180)
                            .height(40.dp)
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        text = "${weather.windSpeedMph} MPH",
                        color = MaterialTheme.colors.primary,
                        style = MaterialTheme.typography.h6
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${weather.windDirectionCardinal} Wind",
                    color = MaterialTheme.colors.secondary,
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }

    @Composable
    private fun TimetableButton(title: String, path: String, serviceID: Int) {
        val containsTimetable = resources.assets.list(path)
            ?.contains("${serviceID}.pdf") ?: false
        if (containsTimetable) {
            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colors.secondaryVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true),
                        onClick = {
                            openPdf("${path}/${serviceID}.pdf")
                        }
                    )
            ) {
                Text(
                    text = title,
                    color = colorResource(id = R.color.colorAccent),
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Left
                )
            }
        }
    }

    @Composable
    private fun LocationsMapView(
        locations: Array<Location>,
        vessels: Array<Vessel>,
        modifier: Modifier
    ) {
        // The MapView lifecycle is handled by this composable. As the MapView also needs to be updated
        // with input from Compose UI, those updates are encapsulated into the MapViewContainer
        // composable. In this way, when an update to the MapView happens, this composable won't
        // recompose and the MapView won't need to be recreated.
        val mapView = rememberMapViewWithLifecycle()
        MapViewContainer(mapView, locations, vessels, modifier)
    }

    @Composable
    private fun MapViewContainer(
        map: MapView,
        locations: Array<Location>,
        vessels: Array<Vessel>,
        modifier: Modifier
    ) {
        LaunchedEffect(Triple(map, locations, vessels)) {
            val context = context ?: return@LaunchedEffect
            val googleMap = map.awaitMap()

            googleMap.uiSettings.setAllGesturesEnabled(false)
            googleMap.setOnMarkerClickListener { true }

            when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(context, R.raw.style_json)
                    )
                }

                else -> {
                    googleMap.setMapStyle(null)
                }
            }

            googleMap.clear()

            vessels.map(::convertVesselToMarkerOptions).forEach { googleMap.addMarker(it) }

            val latLngBuilder = LatLngBounds.Builder()
            locations.map { location ->
                MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .title(location.name)
            }.forEach {
                latLngBuilder.include(it.position)
                googleMap.addMarker(it)
            }

            try {
                val width = resources.displayMetrics.widthPixels
                val height = resources.displayMetrics.heightPixels
                val padding = min(width, height) * 0.15
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(latLngBuilder.build(), padding.toInt())
                )
            } catch (exception: Throwable) {
                Sentry.captureException(exception)
            }
        }

        AndroidView({ map }, modifier = modifier)
    }

    private fun openPdf(file: String) {
        val context = context ?: return

        val timetablesPath = File(context.filesDir, "timetables")
        timetablesPath.mkdir()
        val timetableFile = File(timetablesPath, "timetable.pdf")

        context.assets?.open(file).use { input ->
            timetableFile.outputStream().use { output ->
                input?.copyTo(output, 1024)
            }
        }

        val timetableUri =
            getUriForFile(context, "com.scottishferryapp.fileprovider", timetableFile)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            setDataAndType(timetableUri, "application/pdf")
        }

        try {
            startActivity(sendIntent)
        } catch (exception: Throwable) {
            // TODO: Show error
            Sentry.captureException(exception)
        }
    }

    private fun navigate(direction: NavDirections) {
        val navController = view?.findNavController()
        if (navController?.currentDestination?.id == R.id.detailFragment) {
            navController.navigate(direction)
        }
    }
}