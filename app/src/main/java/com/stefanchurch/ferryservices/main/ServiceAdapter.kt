package com.stefanchurch.ferryservices.main

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.stefanchurch.ferryservices.R
import com.stefanchurch.ferryservices.databinding.ServiceItemBinding
import com.stefanchurch.ferryservices.models.Service
import com.stefanchurch.ferryservices.models.Status

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
            val statusColor = when (Status.values().firstOrNull { it.value == item.status } ?: Status.UNKNOWN) {
                Status.NORMAL -> ContextCompat.getColor(binding.root.context,
                    R.color.colorStatusNormal
                )
                Status.DISRUPTED -> ContextCompat.getColor(binding.root.context,
                    R.color.colorStatusDisrupted
                )
                Status.CANCELLED -> ContextCompat.getColor(binding.root.context,
                    R.color.colorStatusCancelled
                )
                Status.UNKNOWN -> ContextCompat.getColor(binding.root.context,
                    R.color.colorStatusUnknown
                )
            }
            binding.statusView.background.setColorFilter(statusColor, PorterDuff.Mode.ADD)
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