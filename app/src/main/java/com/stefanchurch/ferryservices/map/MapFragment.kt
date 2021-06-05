package com.stefanchurch.ferryservices.map

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.stefanchurch.ferryservices.databinding.MapFragmentBinding

class MapFragment : Fragment() {

    private val args: MapFragmentArgs by navArgs()

    private var mapView: MapView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = MapFragmentBinding.inflate(inflater, container, false)

        mapView = binding.mapView
        mapView?.onCreate(null)
        mapView?.getMapAsync { map ->
            val markers = args.service.locations.map { location ->
                MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .title(location.name)
            }

            val builder = LatLngBounds.Builder()
            markers.forEach {
                map.addMarker(it)
                builder.include(it.position)
            }

            map.moveCamera(
                CameraUpdateFactory.newLatLngBounds(builder.build(), 200)
            )
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