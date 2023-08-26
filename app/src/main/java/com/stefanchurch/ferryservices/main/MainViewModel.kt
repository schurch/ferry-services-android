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

    private var services: Array<Service> = defaultServices
    private var searchText: String = ""

    init {
        _items = mutableStateOf(convertServicesToRows(getSubscribedServicesIDs(), services, searchText))
        _isRefreshing = mutableStateOf(false)
    }

    fun refresh() {
        _isRefreshing.value = true

        viewModelScope.launch {
            try {
                services = servicesRepository.getServices()
                updateRows()
            }
            catch (exception: Throwable) {
                //TODO: Error handling
            }

            _isRefreshing.value = false
        }
    }

    fun updateSearchText(searchText: String) {
        this.searchText = searchText
        updateRows()
    }

    private fun updateRows() {
        _items.value = convertServicesToRows(getSubscribedServicesIDs(), services, searchText)
    }

    private fun getSubscribedServicesIDs() : List<Int> {
        return preferences.lookupString(R.string.preferences_subscribed_services_key)?.let {
            Json.decodeFromString(it) as List<Int>
        } ?: listOf()
    }

}

private fun convertServicesToRows(
    subscribedServiceIDs: List<Int>,
    services: Array<Service>,
    searchText: String
) : List<ServiceItem> {
     if (searchText.isNotEmpty()) {
        return services
            .filter { service ->
                service.area.lowercase().contains(searchText.lowercase()) ||
                        service.route.lowercase().contains(searchText.lowercase())
            }
            .map {
                ServiceItem.ServiceItemService(it)
            }
    }

    val groupedServices = groupServicesByOperator(services = services.toList()).flatMap { operatorServices ->
        val header = ServiceItem.ServiceItemHeader(operatorServices.first().serviceOperator?.name ?: "Services")
        val servicesItems = operatorServices.map { ServiceItem.ServiceItemService(it) }
        listOf(header) + servicesItems
    }

    val subscribedServices = services.filter { subscribedServiceIDs.contains(it.serviceID) }
    return if (subscribedServices.isNotEmpty()) {
        listOf(ServiceItem.ServiceItemHeader("Subscribed")) + subscribedServices.map { ServiceItem.ServiceItemService(it) } + groupedServices
    } else {
        groupedServices
    }
}

fun groupServicesByOperator(services: List<Service>): List<List<Service>> {
    return services
        .groupBy { it.serviceOperator?.id ?: 0 }
        .values
        .sortedBy { it.first().serviceOperator?.name }
}