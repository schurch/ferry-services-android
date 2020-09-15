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
import com.stefanchurch.ferryservices.API
import com.stefanchurch.ferryservices.InstallationID
import com.stefanchurch.ferryservices.R
import com.stefanchurch.ferryservices.databinding.DetailFragmentBinding
import com.stefanchurch.ferryservices.models.statusColor

class DetailFragment : Fragment() {

    private val model: DetailViewModel by viewModels {
        DetailViewModelFactory(
            args.service,
            API.getInstance(requireContext().applicationContext),
            InstallationID.getInstallationID(requireContext().applicationContext),
            this
        )
    }

    private val args: DetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<DetailFragmentBinding>(inflater, R.layout.detail_fragment, container, false)

        binding.model = model
        binding.lifecycleOwner = viewLifecycleOwner

        binding.statusView.background.setColorFilter(args.service.statusColor(binding.root.context), PorterDuff.Mode.SRC_ATOP)

        binding.subscribeSwitch.setOnCheckedChangeListener { _, isChecked ->
            model.updatedSubscribedStatus(isChecked)
        }

        model.navigateToAdditionalInfo = { direction ->
            view?.findNavController()?.navigate(direction)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        model.getSubscribedStatus()
    }
}