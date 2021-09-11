package com.stefanchurch.ferryservices.detail

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.stefanchurch.ferryservices.ServicesRepository
import com.stefanchurch.ferryservices.R
import com.stefanchurch.ferryservices.SharedPreferences
import com.stefanchurch.ferryservices.databinding.DetailFragmentBinding
import com.stefanchurch.ferryservices.models.Service
import com.stefanchurch.ferryservices.models.statusColor
import io.sentry.Sentry

class DetailFragment : Fragment() {

    private val model: DetailViewModel by viewModels {
        DetailViewModelFactory(
            args.serviceDetailArgument,
            ServicesRepository.getInstance(requireContext().applicationContext),
            SharedPreferences(requireContext().applicationContext),
            this
        )
    }

    private val args: DetailFragmentArgs by navArgs()
//    private var mapView: MapView? = null
//    private var map: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<DetailFragmentBinding>(inflater, R.layout.detail_fragment, container, false)

//        binding.model = model
        binding.lifecycleOwner = viewLifecycleOwner
        binding.detailScreen.setContent {
            DetailScreen(args.serviceDetailArgument.service)
        }
//        binding.subscribeSwitch.setOnCheckedChangeListener { _, isChecked ->
//            model.updatedSubscribedStatus(isChecked)
//        }
//
//        model.navigateToWithDirection = { direction ->
//            val navController = view?.findNavController()
//            if (navController?.currentDestination?.id == R.id.detailFragment) {
//                navController?.navigate(direction)
//            }
//        }
//
//        model.setColor = { service ->
//            binding.statusView.background.setColorFilter(service.statusColor(binding.root.context), PorterDuff.Mode.SRC_ATOP)
//        }
//
//        model.configureMap = { service ->
//            configureMap(service)
//        }
//
//        mapView = binding.mapView
//        mapView?.onCreate(null)
//        mapView?.getMapAsync {
//            map = it
//            model.service?.let { service ->
//                configureMap(service)
//            }
//        }
//
//        model.configureView()

        return binding.root
    }

    @Composable
    private fun DetailScreen(service: Service?) {
        val checkedState = remember { mutableStateOf(true) }

        service?.let { service ->
            Column(modifier = Modifier.padding(all = 10.dp)) {
                Text(
                    text = service.area,
                    fontSize = 30.sp,
                    color = androidx.compose.ui.graphics.Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = service.route,
                    fontSize = 20.sp,
                    color = androidx.compose.ui.graphics.Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(25.dp), onDraw = {
                        drawCircle(color = androidx.compose.ui.graphics.Color.Red)
                    })
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        text = "There are currently no disruptions with this service",
                        fontSize = 18.sp,
                        color = androidx.compose.ui.graphics.Color.Gray
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
                        color = androidx.compose.ui.graphics.Color.Gray
                    )
                    Switch(
                        checked = checkedState.value,
                        onCheckedChange = { checkedState.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = colorResource(id = R.color.colorAccent))
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = colorResource(id = R.color.colorAccent),
                        contentColor = androidx.compose.ui.graphics.Color.White
                    )
                ) {
                    Text("ADDITIONAL INFO")
                }
            }
        } ?: run {
            Text(text = "Loading...")
        }
    }

//    private fun configureMap(service: Service) {
//        if (service.locations.count() == 0) return
//
//        val map = map?.let { it } ?: return
//
//        map.uiSettings.setAllGesturesEnabled(false)
//        map.setOnMarkerClickListener { true }
//
//        val markers = service.locations.map { location ->
//            MarkerOptions()
//                .position(LatLng(location.latitude, location.longitude))
//                .title(location.name)
//        }
//
//        val builder = LatLngBounds.Builder()
//        markers.forEach {
//            map.addMarker(it)
//            builder.include(it.position)
//        }
//
//        try {
//            map.moveCamera(
//                CameraUpdateFactory.newLatLngBounds(builder.build(), 90)
//            )
//        } catch (exception: Throwable) {
//            Sentry.captureException(exception)
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        mapView?.onResume()
//        model.getSubscribedStatus()
//    }
//
//    override fun onPause() {
//        super.onPause()
//
//        mapView?.onPause()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//
//        mapView?.onDestroy()
//    }
//
//    override fun onLowMemory() {
//        super.onLowMemory()
//
//        mapView?.onLowMemory()
//    }
}