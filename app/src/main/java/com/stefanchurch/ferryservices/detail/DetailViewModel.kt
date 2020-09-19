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

    fun getSubscribedStatus() {
        if (installationID == null) return

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
        if (installationID == null) return

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

private val Service.statusText: Int
    get() = when (status) {
        Status.NORMAL -> R.string.status_normal
        Status.DISRUPTED -> R.string.status_disrupted
        Status.CANCELLED -> R.string.status_cancelled
        Status.UNKNOWN -> R.string.status_unknown
    }