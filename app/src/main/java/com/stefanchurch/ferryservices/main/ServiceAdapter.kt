package com.stefanchurch.ferryservices.main

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.stefanchurch.ferryservices.databinding.ServiceItemBinding
import com.stefanchurch.ferryservices.databinding.ServiceItemHeaderBinding
import com.stefanchurch.ferryservices.detail.ServiceDetailArgument
import com.stefanchurch.ferryservices.models.Service
import com.stefanchurch.ferryservices.models.statusColor
import java.lang.Exception

private const val ITEM_TYPE_HEADER = 1
private const val ITEM_TYPE_SERVICE = 2

class ServicesAdapter(var rows: List<ServiceItem> = listOf()) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_HEADER ->  HeaderViewHolder(
                ServiceItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            ITEM_TYPE_SERVICE -> ServiceViewHolder(
                ServiceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> throw Exception()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is ServiceItem.ServiceItemHeader -> (holder as HeaderViewHolder).bind(row.text)
            is ServiceItem.ServiceItemService -> (holder as ServiceViewHolder).bind(row.service)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (rows[position]) {
            is ServiceItem.ServiceItemHeader -> ITEM_TYPE_HEADER
            is ServiceItem.ServiceItemService -> ITEM_TYPE_SERVICE
        }
    }

    override fun getItemCount() = rows.size
}

private class ServiceViewHolder(private val binding: ServiceItemBinding) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.setClickListener { view ->
            binding.service?.let { service ->
                val direction = MainFragmentDirections.actionMainFragmentToServiceDetail(ServiceDetailArgument(service.serviceID, service))
                view.findNavController().navigate(direction)
            }
        }
    }

    fun bind(item: Service) {
        binding.statusView.background.setColorFilter(
            item.statusColor(binding.root.context),
            PorterDuff.Mode.SRC_ATOP
        )
        binding.service = item
        binding.executePendingBindings()
    }
}

private class HeaderViewHolder(private val binding: ServiceItemHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(text: String) {
        binding.textView.text = text
    }
}
