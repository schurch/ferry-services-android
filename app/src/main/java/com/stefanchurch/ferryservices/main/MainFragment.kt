package com.stefanchurch.ferryservices.main

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stefanchurch.ferryservices.ServicesRepository
import com.stefanchurch.ferryservices.R
import com.stefanchurch.ferryservices.SharedPreferences
import com.stefanchurch.ferryservices.databinding.MainFragmentBinding
import com.stefanchurch.ferryservices.models.Service
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MainFragment : Fragment() {

    private val model: MainViewModel by viewModels {
        val json = resources.assets.open("services.json").bufferedReader().use { it.readText() }
        val format = Json { ignoreUnknownKeys = true }
        val defaultServices = format.decodeFromString<Array<Service>>(json)

        MainViewModelFactory(
            defaultServices,
            ServicesRepository.getInstance(requireContext().applicationContext),
            SharedPreferences(requireContext().applicationContext),
            this
        )
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: ServicesAdapter
    private lateinit var viewManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MainFragmentBinding.inflate(inflater, container, false)
        context ?: return binding.root

        viewManager = LinearLayoutManager(context)
        viewAdapter = ServicesAdapter()

        recyclerView = binding.recyclerview.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        model.rows.observe(viewLifecycleOwner) { rows ->
            viewAdapter.rows = rows
            viewAdapter.notifyDataSetChanged()
        }

        model.showError = { error ->
            val builder = AlertDialog.Builder(binding.root.context)
            builder.setMessage(error)
                .setPositiveButton(R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                }
            builder.create().show()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        model.reloadServices()
    }
}
