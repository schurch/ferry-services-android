package com.stefanchurch.ferryservices.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stefanchurch.ferryservices.API
import com.stefanchurch.ferryservices.Preferences
import com.stefanchurch.ferryservices.R
import com.stefanchurch.ferryservices.models.Service
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

sealed class MainRow {
    data class HeaderRow(val text: String) : MainRow()
    data class ServiceRow(val service: Service) : MainRow()
}

class MainViewModel(
    private val api: API,
    private val preferences: Preferences,
    var showError: ((String) -> Unit)? = null
) : ViewModel() {

    val rows: MutableLiveData<List<MainRow>> by lazy {
        MutableLiveData<List<MainRow>>()
    }

    private var services: Array<Service>? = null

    fun reloadServices() {
        viewModelScope.launch {
            try {
                // If we have a list of services already, then update service rows before API call if we've subscribed or unsubscribed
                updateServicesRows()
                services = api.getServices()
                updateServicesRows()
            }
            catch (e: Throwable) {
                showError?.invoke("There was a problem updating the services. Please try again later.")
            }
        }
    }

    private fun updateServicesRows() {
        val services = services?.let { it } ?: return

        val subscribedServicesIDs = preferences.lookupString(R.string.preferences_subscribed_services_key)?.let { Json.decodeFromString(it) as List<Int> } ?: listOf()
        val subscribedServices = subscribedServicesIDs.mapNotNull { services.find { service -> service.serviceID == it} }
        if (subscribedServices.isNotEmpty()) {
            rows.value = listOf(MainRow.HeaderRow("Subscribed")) +
                    subscribedServices.map { MainRow.ServiceRow(it) } +
                    listOf(MainRow.HeaderRow("Services")) +
                    services.map { MainRow.ServiceRow(it) }
        } else {
            rows.value = services.map { MainRow.ServiceRow(it) }
        }
    }

}