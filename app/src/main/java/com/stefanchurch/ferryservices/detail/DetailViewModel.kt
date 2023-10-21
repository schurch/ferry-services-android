package com.stefanchurch.ferryservices.detail

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stefanchurch.ferryservices.ServicesRepository
import com.stefanchurch.ferryservices.Preferences
import com.stefanchurch.ferryservices.R
import com.stefanchurch.ferryservices.models.Service
import com.stefanchurch.ferryservices.models.Vessel
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
class DetailViewModel(
    serviceDetailArgument: ServiceDetailArgument,
    private val servicesRepository: ServicesRepository,
    private val preferences: Preferences
) : ViewModel()  {

    private val _service: MutableState<Service?> = mutableStateOf(null)
    val service: State<Service?>
        get() = _service

    private val _subscribedEnabled = mutableStateOf(true)
    val subscribedEnabled: State<Boolean>
        get() = _subscribedEnabled

    private val _isSubscribed = mutableStateOf(false)
    val isSubscribed: State<Boolean>
        get() = _isSubscribed

    var date: Long = Instant.now().toEpochMilli()

    private val serviceID: Int = serviceDetailArgument.serviceID
    private val installationID = preferences.lookupString(R.string.preferences_installation_id_key)?.let { UUID.fromString(it) }

    init {
        if (serviceDetailArgument.service != null) {
            _service.value = serviceDetailArgument.service
        } else {
            refresh()
        }

        _isSubscribed.value = preferences.lookupString(R.string.preferences_subscribed_services_key)?.let {
            Json.decodeFromString<List<Int>>(it).contains(serviceID)
        } ?: false
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                val service = servicesRepository.getService(serviceID = serviceID, date = date)
                _service.value = service
            } catch (exception: Throwable) {
                //TODO: Error handling
            }
        }
    }

    fun updatedSubscribedStatus(subscribed: Boolean) {
        if (installationID == null) return

        _subscribedEnabled.value = false

        viewModelScope.launch {
            try {
                if (subscribed) {
                    servicesRepository.addService(installationID , serviceID)
                    _isSubscribed.value = true
                    addSubscribedServiceToPrefs()
                }
                else {
                    servicesRepository.removeService(installationID , serviceID)
                    _isSubscribed.value = false
                    removeSubscribedServiceFromPrefs()
                }

                _subscribedEnabled.value = true
            }
            catch (exception: Throwable) {
                _isSubscribed.value = !subscribed
                _subscribedEnabled.value = true
            }
        }
    }

    private fun addSubscribedServiceToPrefs() {
        preferences.lookupString(R.string.preferences_subscribed_services_key)?.let {
            val serviceIDs: List<Int> = Json.decodeFromString(it)
            if (!serviceIDs.contains(serviceID)) {
                val updatedList = listOf(serviceID) + serviceIDs
                preferences.writeString(R.string.preferences_subscribed_services_key, Json.encodeToString(updatedList))
            }
        } ?: run {
            preferences.writeString(R.string.preferences_subscribed_services_key, Json.encodeToString(listOf(serviceID)))
        }
    }

    private fun removeSubscribedServiceFromPrefs() {
        preferences.lookupString(R.string.preferences_subscribed_services_key)?.let {
            val serviceIDs  = Json.decodeFromString<List<Int>>(it).toMutableList()
            if (serviceIDs.contains(serviceID)) {
                serviceIDs.remove(serviceID)
                preferences.writeString(R.string.preferences_subscribed_services_key, Json.encodeToString(serviceIDs))
            }
        }
    }
}
