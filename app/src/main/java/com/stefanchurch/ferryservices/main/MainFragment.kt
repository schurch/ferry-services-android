package com.stefanchurch.ferryservices.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stefanchurch.ferryservices.API
import com.stefanchurch.ferryservices.databinding.MainFragmentBinding

class MainFragment : Fragment() {

    private val model: MainViewModel by viewModels {
        MainViewModelFactory(API.getInstance(requireContext().applicationContext), this)
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: ServicesAdapter
    private lateinit var viewManager: LinearLayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = MainFragmentBinding.inflate(inflater, container, false)
        context ?: return binding.root

        viewManager = LinearLayoutManager(context)
        viewAdapter = ServicesAdapter()

        recyclerView = binding.recyclerview.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(DividerItemDecoration(context, viewManager.orientation))
        }

        model.services.observe(viewLifecycleOwner) { services ->
            viewAdapter.services = services
            viewAdapter.notifyDataSetChanged()
        }

        return binding.root
    }

}
