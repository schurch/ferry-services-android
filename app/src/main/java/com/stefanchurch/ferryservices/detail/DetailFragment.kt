package com.stefanchurch.ferryservices.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.stefanchurch.ferryservices.databinding.DetailsFragmentBinding

class DetailFragment : Fragment() {

    private val args: DetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DetailsFragmentBinding.inflate(inflater, container, false)
        context ?: return binding.root

        binding.textView.text = args.service.route

        return binding.root
    }

}