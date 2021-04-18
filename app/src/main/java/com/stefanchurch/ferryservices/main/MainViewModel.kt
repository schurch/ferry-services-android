package com.stefanchurch.ferryservices.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stefanchurch.ferryservices.ServicesRepository
import com.stefanchurch.ferryservices.Preferences
import com.stefanchurch.ferryservices.R
import com.stefanchurch.ferryservices.models.Service
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MainViewModel(
    private val servicesRepository: ServicesRepository,
    private val preferences: Preferences,
    var showError: ((String) -> Unit)? = null
) : ViewModel() {

    val rows: MutableLiveData<List<ServiceItem>> by lazy {
        MutableLiveData<List<ServiceItem>>()
    }

    private var services: Array<Service>? = null

    fun reloadServices() {
        viewModelScope.launch {
            try {
                // If we have a list of services already, then update service rows before API call if we've subscribed or unsubscribed
                updateServicesRows()
                services = servicesRepository.getServices()
                updateServicesRows()
            }
            catch (e: Throwable) {
                showError?.invoke("There was a problem updating the services. Please try again later.")
            }
        }
    }

    private fun updateServicesRows() {
        val services = services ?: return

        val subscribedServicesIDs = preferences.lookupString(R.string.preferences_subscribed_services_key)?.let { Json.decodeFromString(it) as List<Int> } ?: listOf()
        val subscribedServices = subscribedServicesIDs.mapNotNull { services.find { service -> service.serviceID == it} }
        if (subscribedServices.isNotEmpty()) {
            rows.value = listOf(ServiceItem.ServiceItemHeader("Subscribed")) +
                    subscribedServices.map { ServiceItem.ServiceItemService(it) } +
                    listOf(ServiceItem.ServiceItemHeader("Services")) +
                    services.map { ServiceItem.ServiceItemService(it) }
        } else {
            rows.value = services.map { ServiceItem.ServiceItemService(it) }
        }
    }

}