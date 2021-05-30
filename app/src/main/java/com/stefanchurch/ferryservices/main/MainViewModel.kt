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
    defaultServices: Array<Service>,
    private val servicesRepository: ServicesRepository,
    private val preferences: Preferences,
) : ViewModel() {

    var showError: ((String) -> Unit)? = null

    val rows: MutableLiveData<List<ServiceItem>> by lazy {
        MutableLiveData<List<ServiceItem>>()
    }

    init {
        rows.value = convertServicesToRows(getSubscribedServicesIDs(), defaultServices)
    }

    fun reloadServices() {
        viewModelScope.launch {
            try {
                val services = servicesRepository.getServices()
                rows.value = convertServicesToRows(getSubscribedServicesIDs(), services)
            }
            catch (e: Throwable) {
                showError?.invoke("There was a problem updating the services. Please try again later.")
            }
        }
    }

    private fun getSubscribedServicesIDs() : List<Int> {
        return preferences.lookupString(R.string.preferences_subscribed_services_key)?.let {
            Json.decodeFromString(it) as List<Int>
        } ?: listOf()
    }

}

private fun convertServicesToRows(subscribedServiceIDs: List<Int>, services: Array<Service>) : List<ServiceItem> {
    val subscribedServices = subscribedServiceIDs
        .mapNotNull { services.find { service -> service.serviceID == it} }
        .sortedBy { it.sortOrder }

    return if (subscribedServices.isNotEmpty()) {
        listOf(ServiceItem.ServiceItemHeader("Subscribed")) +
                subscribedServices.map { ServiceItem.ServiceItemService(it) } +
                listOf(ServiceItem.ServiceItemHeader("Services")) +
                services.map { ServiceItem.ServiceItemService(it) }
    } else {
        services.map { ServiceItem.ServiceItemService(it) }
    }
}