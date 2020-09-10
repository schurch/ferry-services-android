package com.stefanchurch.ferryservices.additional

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.stefanchurch.ferryservices.databinding.AdditionalFragmentBinding

class AdditionalFragment: Fragment() {

    private val args: AdditionalFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = AdditionalFragmentBinding.inflate(inflater, container, false)

        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        binding.webview.loadData(args.service.additionalInfo!!, "text/html; charset=utf-8", "UTF-8")

        return binding.root
    }
}