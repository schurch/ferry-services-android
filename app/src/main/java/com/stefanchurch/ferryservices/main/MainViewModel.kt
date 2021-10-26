package com.stefanchurch.ferryservices.main

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
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

    private val _items: MutableState<List<ServiceItem>>
    val items: State<List<ServiceItem>>
        get() = _items

    private val _isRefreshing: MutableState<Boolean>
    val isRefreshing: State<Boolean>
        get() = _isRefreshing

    init {
        _items = mutableStateOf(convertServicesToRows(getSubscribedServicesIDs(), defaultServices))
        _isRefreshing = mutableStateOf(false)
    }

    fun refresh() {
        _isRefreshing.value = true

        viewModelScope.launch {
            try {
                val services = servicesRepository.getServices()
                _items.value = convertServicesToRows(getSubscribedServicesIDs(), services)
            }
            catch (exception: Throwable) {
                //TODO: Error handling
            }

            _isRefreshing.value = false
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