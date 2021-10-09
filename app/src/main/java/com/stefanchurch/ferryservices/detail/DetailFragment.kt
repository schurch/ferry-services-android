package com.stefanchurch.ferryservices.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider.getUriForFile
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.stefanchurch.ferryservices.ServicesRepository
import com.stefanchurch.ferryservices.R
import com.stefanchurch.ferryservices.SharedPreferences
import com.stefanchurch.ferryservices.databinding.DetailFragmentBinding
import com.stefanchurch.ferryservices.models.Status
import com.stefanchurch.ferryservices.models.status
import com.stefanchurch.ferryservices.rememberMapViewWithLifecycle
import com.google.maps.android.ktx.awaitMap
import com.stefanchurch.ferryservices.models.Location
import io.sentry.Sentry
import java.io.File
import kotlin.math.min

class DetailFragment : Fragment() {

    private val args: DetailFragmentArgs by navArgs()

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
            DetailScreen()
        }

        return binding.root
    }

    @Composable
    private fun DetailScreen(
        viewModel: DetailViewModel = viewModel(
            viewModelStoreOwner = this,
            key = null,
            factory = DetailViewModelFactory(
                args.serviceDetailArgument,
                ServicesRepository.getInstance(requireContext().applicationContext),
                SharedPreferences(requireContext().applicationContext),
                this
            )
        )
    ) {
        viewModel.service.value?.let { service ->
            Column(
                modifier =
                Modifier
                    .padding(all = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    Modifier
                        .height(200.dp)
                        .fillMaxWidth()) {
                    LocationsMapView(
                        locations = service.locations,
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
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = service.area,
                    fontSize = 30.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = service.route,
                    fontSize = 20.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(20.dp))

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
                        fontSize = 18.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (hasAdditionalInfo) {
                        Spacer(modifier = Modifier.width(15.dp))
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            tint = Color.Gray,
                            contentDescription = "Additional Info"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Subscribe to updates",
                        fontSize = 18.sp,
                        color = Color.Gray
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

                val winterPath = "Timetables/2021/Winter"
                val containsWinterTimetable = resources.assets.list(winterPath)
                    ?.contains("${service.serviceID}.pdf") ?: false
                if (containsWinterTimetable) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(35.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(bounded = true),
                                onClick = {
                                    openPdf("${winterPath}/${service.serviceID}.pdf")
                                }
                            )
                    ) {
                        Text(
                            text = "VIEW WINTER 2021–2022 TIMETABLE",
                            textAlign = TextAlign.Left,
                            fontSize = 18.sp,
                            color = colorResource(id = R.color.colorAccent)
                        )
                    }
                }

                val summerPath = "Timetables/2021/Summer"
                val containsSummerTimetable = resources.assets.list(summerPath)
                    ?.contains("${service.serviceID}.pdf") ?: false
                if (containsSummerTimetable) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(35.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(bounded = true),
                                onClick = {
                                    openPdf("${summerPath}/${service.serviceID}.pdf")
                                }
                            )
                    ) {
                        Text(
                            text = "VIEW SUMMER 2021 TIMETABLE",
                            textAlign = TextAlign.Left,
                            fontSize = 18.sp,
                            color = colorResource(id = R.color.colorAccent)
                        )
                    }
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
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }
        }
    }

    @Composable
    private fun LocationsMapView(locations: Array<Location>, modifier: Modifier) {
        // The MapView lifecycle is handled by this composable. As the MapView also needs to be updated
        // with input from Compose UI, those updates are encapsulated into the MapViewContainer
        // composable. In this way, when an update to the MapView happens, this composable won't
        // recompose and the MapView won't need to be recreated.
        val mapView = rememberMapViewWithLifecycle()
        MapViewContainer(mapView, locations, modifier)
    }

    @Composable
    private fun MapViewContainer(
        map: MapView,
        locations: Array<Location>,
        modifier: Modifier
    ) {
        val mapData = remember(locations) {
            val markers = locations.map { location ->
                MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .title(location.name)
            }

            val builder = LatLngBounds.Builder()
            markers.forEach {
                builder.include(it.position)
            }

            Pair(markers, builder.build())
        }

        LaunchedEffect(map) {
            val googleMap = map.awaitMap()

            googleMap.uiSettings.setAllGesturesEnabled(false)
            googleMap.setOnMarkerClickListener { true }

            val (markers, mapBounds) = mapData
            markers.forEach {
                googleMap.addMarker(it)
            }

            try {
                val width = resources.displayMetrics.widthPixels
                val height = resources.displayMetrics.heightPixels
                val padding = min(width, height) * 0.15
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(mapBounds, padding.toInt())
                )
            } catch (exception: Throwable) {
                Sentry.captureException(exception)
            }
        }

        AndroidView({ map }, modifier = modifier)
    }

    private fun openPdf(file: String) {
        val context = context?.let { it } ?: return

        val timetablesPath = File(context.filesDir, "timetables")
        timetablesPath.mkdir()
        val timetableFile = File(timetablesPath, "timetable.pdf")

        context.assets?.open(file).use { input ->
            timetableFile.outputStream().use { output ->
                input?.copyTo(output, 1024)
            }
        }

        val timetableUri = getUriForFile(context, "com.scottishferryapp.fileprovider", timetableFile)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_VIEW
            type = "application/pdf"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            data = timetableUri
        }
        startActivity(sendIntent)
    }

    private fun navigate(direction: NavDirections) {
        val navController = view?.findNavController()
        if (navController?.currentDestination?.id == R.id.detailFragment) {
            navController.navigate(direction)
        }
    }
}