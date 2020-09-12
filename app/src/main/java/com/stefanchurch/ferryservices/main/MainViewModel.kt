package com.stefanchurch.ferryservices.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stefanchurch.ferryservices.API
import com.stefanchurch.ferryservices.models.Service
import kotlinx.coroutines.launch

class MainViewModel(
    private val api: API,
    var showError: ((String) -> Unit)? = null
) : ViewModel() {

    val services: MutableLiveData<Array<Service>> by lazy {
        MutableLiveData<Array<Service>>()
    }

    fun reloadServices() {
        viewModelScope.launch {
            try {
                services.value = api.getServices()
            }
            catch (e: Throwable) {
                showError?.invoke("There was a problem updating the services. Please try again later.")
            }
        }
    }

}