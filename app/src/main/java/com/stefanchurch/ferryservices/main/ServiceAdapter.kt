package com.stefanchurch.ferryservices.main

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.stefanchurch.ferryservices.databinding.ServiceItemBinding
import com.stefanchurch.ferryservices.models.Service
import com.stefanchurch.ferryservices.models.statusColor

class ServicesAdapter(var services: Array<Service> = arrayOf()) : RecyclerView.Adapter<ServicesAdapter.ServiceViewHolder>() {

    class ServiceViewHolder(private val binding: ServiceItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.setClickListener { view ->
                binding.service?.let { service ->
                    val direction = MainFragmentDirections.actionMainFragmentToPlaceholder(service)
                    view.findNavController().navigate(direction)
                }
            }
        }

        fun bind(item: Service) {
            binding.statusView.background.setColorFilter(item.statusColor(binding.root.context), PorterDuff.Mode.SRC_ATOP)
            binding.service = item
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        return ServiceViewHolder(
            ServiceItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(services[position])
    }

    override fun getItemCount() = services.size
}