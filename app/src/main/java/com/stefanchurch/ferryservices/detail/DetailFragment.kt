package com.stefanchurch.ferryservices.detail

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.stefanchurch.ferryservices.R
import com.stefanchurch.ferryservices.databinding.DetailFragmentBinding
import com.stefanchurch.ferryservices.main.MainFragmentDirections
import com.stefanchurch.ferryservices.models.Status

class DetailFragment : Fragment() {

    private val args: DetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<DetailFragmentBinding>(
            inflater,
            R.layout.detail_fragment,
            container,
            false
        ).apply {
            service = args.service
            lifecycleOwner = viewLifecycleOwner

            mapView.onCreate(savedInstanceState)
            mapView.onResume()

            mapView.getMapAsync { map ->
                 map.setPadding(0, 150, 0, 0)

                 service?.let { it ->
                     val markers = it.locations.map { location ->
                         MarkerOptions()
                             .position(LatLng(location.latitude, location.longitude))
                             .title(location.name)
                     }

                     val boundsBuilder = LatLngBounds.builder()
                     markers?.forEach {
                         map.addMarker(it)
                         boundsBuilder.include(it.position)
                     }

                     map.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
                }
            }

            var isToolbarShown = false

            // scroll change listener begins at Y = 0 when map is fully collapsed
            detailScrollview.setOnScrollChangeListener(
                NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->

                    // User scrolled past map to height of toolbar and the title text is
                    // underneath the toolbar, so the toolbar should be shown.
                    val shouldShowToolbar = scrollY > toolbar.height

                    // The new state of the toolbar differs from the previous state; update
                    // appbar and toolbar attributes.
                    if (isToolbarShown != shouldShowToolbar) {
                        isToolbarShown = shouldShowToolbar
                        // Use shadow animator to add elevation if toolbar is shown
                        appbar.isActivated = shouldShowToolbar
                    }
                }
            )

            toolbar.setNavigationOnClickListener { view ->
                view.findNavController().navigateUp()
            }

            val status = Status.values().firstOrNull { it.value == args.service.status } ?: Status.UNKNOWN

            val statusColor = when (status) {
                Status.NORMAL -> ContextCompat.getColor(root.context,
                    R.color.colorStatusNormal
                )
                Status.DISRUPTED -> ContextCompat.getColor(root.context,
                    R.color.colorStatusDisrupted
                )
                Status.CANCELLED -> ContextCompat.getColor(root.context,
                    R.color.colorStatusCancelled
                )
                Status.UNKNOWN -> ContextCompat.getColor(root.context,
                    R.color.colorStatusUnknown
                )
            }

            statusView.background.setColorFilter(statusColor, PorterDuff.Mode.ADD)

            val statusText = when (status) {
                Status.NORMAL -> "There are currently no disruptions with this service"
                Status.DISRUPTED -> "There are disruptions with this service"
                Status.CANCELLED -> "Sailings have been cancelled for this service"
                Status.UNKNOWN -> "Unable to fetch the disruption status for this service"
            }

            statusTextView.text = statusText

            button.setOnClickListener {
                service?.let {
                    val direction = DetailFragmentDirections.actionDetailFragmentToAdditional(it)
                    view?.findNavController()?.navigate(direction)
                }
            }
        }

        return binding.root
    }

}