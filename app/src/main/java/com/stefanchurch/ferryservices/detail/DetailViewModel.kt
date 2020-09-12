package com.stefanchurch.ferryservices.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.stefanchurch.ferryservices.models.Service
import com.stefanchurch.ferryservices.models.Status
import com.stefanchurch.ferryservices.models.status

class DetailViewModel(
    val service: Service,
    val area: MutableLiveData<String> = MutableLiveData(service.area),
    val route: MutableLiveData<String> = MutableLiveData(service.route),
    val statusText: MutableLiveData<String> = MutableLiveData(service.statusText),
    val additionalInfoVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>(service.additionalInfo?.isNotEmpty()),
    var navigateToAdditionalInfo: ((NavDirections) -> Unit)? = null
) : ViewModel()  {

    fun navigateToAdditionalInfo() {
        val direction = DetailFragmentDirections.actionDetailFragmentToAdditional(service)
        navigateToAdditionalInfo?.invoke(direction)
    }
}

val Service.statusText: String
    get() = when (status) {
        Status.NORMAL -> "There are currently no disruptions with this service"
        Status.DISRUPTED -> "There are disruptions with this service"
        Status.CANCELLED -> "Sailings have been cancelled for this service"
        Status.UNKNOWN -> "Unable to fetch the disruption status for this service"
    }