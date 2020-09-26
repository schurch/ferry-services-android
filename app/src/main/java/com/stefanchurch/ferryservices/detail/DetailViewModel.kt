package com.stefanchurch.ferryservices.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.stefanchurch.ferryservices.API
import com.stefanchurch.ferryservices.Preferences
import com.stefanchurch.ferryservices.R
import com.stefanchurch.ferryservices.models.Service
import com.stefanchurch.ferryservices.models.Status
import com.stefanchurch.ferryservices.models.status
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class DetailViewModel(
    val service: Service,
    val api: API,
    private val preferences: Preferences,
    val area: String = service.area,
    val route: String = service.route,
    val statusText: MutableLiveData<Int> = MutableLiveData(service.statusText),
    val additionalInfoVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>(service.additionalInfo?.isNotEmpty()),
    val isSubscribed: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false),
    val isSubscribedEnabled: MutableLiveData<Boolean> = MutableLiveData<Boolean>(preferences.lookupBool(R.string.preferences_created_installation_key)),
    var navigateToAdditionalInfo: ((NavDirections) -> Unit)? = null
) : ViewModel()  {

    private val installationID = preferences.lookupString(R.string.preferences_installation_id_key)?.let { UUID.fromString(it) }

    init {
        isSubscribed.value = preferences.lookupString(R.string.preferences_subscribed_services_key)?.let {
            Json.decodeFromString<List<Int>>(it).contains(service.serviceID)
        } ?: false
    }

    fun getSubscribedStatus() {
        if (installationID == null) return

        viewModelScope.launch {
            try {
                val services = api.getSubscribedServices(installationID)
                if (services.map { it.serviceID }.contains(service.serviceID)) {
                    isSubscribed.value = true
                    addSubscribedServiceToPrefs()
                } else {
                    isSubscribed.value = false
                    removeSubscribedServiceFromPrefs()
                }

                isSubscribedEnabled.value = true
            }
            catch (e: Throwable) {

            }
        }
    }

    fun updatedSubscribedStatus(subscribed: Boolean) {
        if (installationID == null) return

        isSubscribedEnabled.value = false

        viewModelScope.launch {
            try {
                if (subscribed) {
                    api.addService(installationID , service.serviceID)
                    addSubscribedServiceToPrefs()
                }
                else {
                    api.removeService(installationID , service.serviceID)
                    removeSubscribedServiceFromPrefs()
                }

                isSubscribedEnabled.value = true
            }
            catch (e: Throwable) {
                isSubscribed.value = !subscribed
                isSubscribedEnabled.value = true
            }
        }
    }

    fun navigateToAdditionalInfo() {
        val direction = DetailFragmentDirections.actionDetailFragmentToAdditional(service)
        navigateToAdditionalInfo?.invoke(direction)
    }

    private fun addSubscribedServiceToPrefs() {
        preferences.lookupString(R.string.preferences_subscribed_services_key)?.let {
            val serviceIDs: List<Int> = Json.decodeFromString(it)
            if (!serviceIDs.contains(service.serviceID)) {
                val updatedList = listOf(service.serviceID) + serviceIDs
                preferences.writeString(R.string.preferences_subscribed_services_key, Json.encodeToString(updatedList))
            }
        } ?: run {
            preferences.writeString(R.string.preferences_subscribed_services_key, Json.encodeToString(listOf(service.serviceID)))
        }
    }

    private fun removeSubscribedServiceFromPrefs() {
        preferences.lookupString(R.string.preferences_subscribed_services_key)?.let {
            val serviceIDs  = Json.decodeFromString<List<Int>>(it).toMutableList()
            if (serviceIDs.contains(service.serviceID)) {
                serviceIDs.remove(service.serviceID)
                preferences.writeString(R.string.preferences_subscribed_services_key, Json.encodeToString(serviceIDs))
            }
        }
    }
}

private val Service.statusText: Int
    get() = when (status) {
        Status.NORMAL -> R.string.status_normal
        Status.DISRUPTED -> R.string.status_disrupted
        Status.CANCELLED -> R.string.status_cancelled
        Status.UNKNOWN -> R.string.status_unknown
    }