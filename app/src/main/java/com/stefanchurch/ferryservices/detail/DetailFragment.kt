package com.stefanchurch.ferryservices.detail

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private var mapView: MapView? = null
    private var map: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<DetailFragmentBinding>(inflater, R.layout.detail_fragment, container, false)

        binding.model = model
        binding.lifecycleOwner = viewLifecycleOwner
        binding.subscribeSwitch.setOnCheckedChangeListener { _, isChecked ->
            model.updatedSubscribedStatus(isChecked)
        }

        model.navigateToWithDirection = { direction ->
            view?.findNavController()?.navigate(direction)
        }

        model.setColor = { service ->
            binding.statusView.background.setColorFilter(service.statusColor(binding.root.context), PorterDuff.Mode.SRC_ATOP)
        }

        model.configureMap = { service ->
            configureMap(service)
        }

        mapView = binding.mapView
        mapView?.onCreate(null)
        mapView?.getMapAsync {
            map = it
            model.service?.let { service ->
                configureMap(service)
            }
        }

        model.configureView()

        return binding.root
    }

    private fun configureMap(service: Service) {
        if (service.locations.count() == 0) return

        val map = map?.let { it } ?: return

        map.uiSettings.setAllGesturesEnabled(false)
        map.setOnMarkerClickListener { true }

        val markers = service.locations.map { location ->
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
            CameraUpdateFactory.newLatLngBounds(builder.build(), 150)
        )
    }

    override fun onResume() {
        super.onResume()

        mapView?.onResume()
        model.getSubscribedStatus()
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