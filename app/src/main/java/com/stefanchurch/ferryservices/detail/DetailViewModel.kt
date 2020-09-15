package com.stefanchurch.ferryservices.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.stefanchurch.ferryservices.API
import com.stefanchurch.ferryservices.models.Service
import com.stefanchurch.ferryservices.models.Status
import com.stefanchurch.ferryservices.models.status
import kotlinx.coroutines.launch
import java.util.*

class DetailViewModel(
    val service: Service,
    val api: API,
    val installationID: UUID,
    val area: MutableLiveData<String> = MutableLiveData(service.area),
    val route: MutableLiveData<String> = MutableLiveData(service.route),
    val statusText: MutableLiveData<String> = MutableLiveData(service.statusText),
    val additionalInfoVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>(service.additionalInfo?.isNotEmpty()),
    val isSubscribed: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false),
    val isSubscribedEnabled: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false),
    var navigateToAdditionalInfo: ((NavDirections) -> Unit)? = null
) : ViewModel()  {

    fun getSubscribedStatus() {
        viewModelScope.launch {
            try {
                val services = api.getSubscribedServices(installationID)
                isSubscribed.value = services.map { it.serviceID }.contains(service.serviceID)
                isSubscribedEnabled.value = true
            }
            catch (e: Throwable) {

            }
        }
    }

    fun updatedSubscribedStatus(subscribed: Boolean) {
        isSubscribedEnabled.value = false

        viewModelScope.launch {
            try {
                if (subscribed) {
                    api.addService(installationID , service.serviceID)
                }
                else {
                    api.removeService(installationID , service.serviceID)
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
}

private val Service.statusText: String
    get() = when (status) {
        Status.NORMAL -> "There are currently no disruptions with this service"
        Status.DISRUPTED -> "There are disruptions with this service"
        Status.CANCELLED -> "Sailings have been cancelled for this service"
        Status.UNKNOWN -> "Unable to fetch the disruption status for this service"
    }