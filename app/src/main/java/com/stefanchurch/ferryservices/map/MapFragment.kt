package com.stefanchurch.ferryservices.map

import android.content.res.Configuration
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.stefanchurch.ferryservices.R
import com.stefanchurch.ferryservices.databinding.MapFragmentBinding
import io.sentry.Sentry
import com.stefanchurch.ferryservices.*

class MapFragment : Fragment() {

    private val args: MapFragmentArgs by navArgs()

    private var mapView: MapView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MapFragmentBinding.inflate(inflater, container, false)

        mapView = binding.mapView
        mapView?.onCreate(null)
        mapView?.getMapAsync { googleMap ->
            val vesselMarkers = (args.service.vessels ?: emptyArray()).map(:: convertVesselToMarkerOptions)
            vesselMarkers.forEach { marker ->
                googleMap.addMarker(marker)
            }

            val locationMarkers = args.service.locations.map { location ->
                MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .title(location.name)
            }

            val builder = LatLngBounds.Builder()
            locationMarkers.forEach {
                googleMap.addMarker(it)
                builder.include(it.position)
            }

            when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            requireContext(),
                            R.raw.style_json
                        )
                    )
                }
                else -> {
                    googleMap.setMapStyle(null)
                }
            }

            try {
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(builder.build(), 90)
                )
            } catch (exception: Throwable) {
                Sentry.captureException(exception)
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()

        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()

        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()

        mapView?.onLowMemory()
    }
}
