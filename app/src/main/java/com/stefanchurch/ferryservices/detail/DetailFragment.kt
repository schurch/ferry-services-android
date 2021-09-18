package com.stefanchurch.ferryservices.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
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
            Column(modifier = Modifier.padding(all = 20.dp)) {
                Box(Modifier.height(200.dp).fillMaxWidth()) {
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
                                val navController = view?.findNavController()
                                if (navController?.currentDestination?.id == R.id.detailFragment) {
                                    val direction = DetailFragmentDirections.actionDetailFragmentToMap(
                                        service = service,
                                        title = service.route
                                    )
                                    navController.navigate(direction)
                                }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
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
                    Text(
                        text = when (service.status) {
                            Status.NORMAL -> "There are currently no disruptions with this service"
                            Status.DISRUPTED -> "There are disruptions with this service"
                            Status.CANCELLED -> "Sailings have been cancelled for this service"
                            Status.UNKNOWN -> "Unable to fetch the disruption status for this service"
                        },
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
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
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val navController = view?.findNavController()
                        if (navController?.currentDestination?.id == R.id.detailFragment) {
                            val direction = DetailFragmentDirections.actionDetailFragmentToAdditional(service)
                            navController.navigate(direction)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = colorResource(id = R.color.colorAccent),
                        contentColor = Color.White
                    )
                ) {
                    Text("ADDITIONAL INFO")
                }
            }
        } ?: run {
            Text(text = "Loading...")
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
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(mapBounds, 90)
                )
            } catch (exception: Throwable) {
                Sentry.captureException(exception)
            }
        }

        AndroidView( { map }, modifier = modifier)
    }
}