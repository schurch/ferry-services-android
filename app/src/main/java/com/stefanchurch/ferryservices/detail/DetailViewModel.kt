package com.stefanchurch.ferryservices.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.stefanchurch.ferryservices.ServicesRepository
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
    serviceDetailArgument: ServiceDetailArgument,
    private val servicesRepository: ServicesRepository,
    private val preferences: Preferences
) : ViewModel()  {

    val area: MutableLiveData<String> = MutableLiveData("")
    val route: MutableLiveData<String> = MutableLiveData("")
    val statusText: MutableLiveData<Int> = MutableLiveData(R.string.empty)
    val additionalInfoVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val isSubscribed: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val isSubscribedEnabled: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    var navigateToAdditionalInfo: ((NavDirections) -> Unit)? = null
    var setColor: ((Service) -> Unit)? = null

    private val serviceID: Int = serviceDetailArgument.serviceID
    private val installationID = preferences.lookupString(R.string.preferences_installation_id_key)?.let { UUID.fromString(it) }
    private var service: Service? = null

    init {
        if (serviceDetailArgument.service != null) {
            service = serviceDetailArgument.service
            configureView()
        } else {
            viewModelScope.launch {
                service = servicesRepository.getService(serviceID)
                configureView()
            }
        }

        isSubscribed.value = preferences.lookupString(R.string.preferences_subscribed_services_key)?.let {
            Json.decodeFromString<List<Int>>(it).contains(serviceID)
        } ?: false
    }

    fun getSubscribedStatus() {
        if (installationID == null) return

        viewModelScope.launch {
            try {
                val services = servicesRepository.getSubscribedServices(installationID)
                if (services.map { it.serviceID }.contains(serviceID)) {
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
                    servicesRepository.addService(installationID , serviceID)
                    addSubscribedServiceToPrefs()
                }
                else {
                    servicesRepository.removeService(installationID , serviceID)
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
        val service = service?.let { it } ?: return

        val direction = DetailFragmentDirections.actionDetailFragmentToAdditional(service)
        navigateToAdditionalInfo?.invoke(direction)
    }

    fun configureView() {
        val service = service?.let { it } ?: return

        area.value = service.area
        route.value = service.route
        statusText.value = service.statusText
        additionalInfoVisible.value = service.additionalInfo?.isNotEmpty()
        setColor?.invoke(service)
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

private val Service.statusText: Int
    get() = when (status) {
        Status.NORMAL -> R.string.status_normal
        Status.DISRUPTED -> R.string.status_disrupted
        Status.CANCELLED -> R.string.status_cancelled
        Status.UNKNOWN -> R.string.status_unknown
    }