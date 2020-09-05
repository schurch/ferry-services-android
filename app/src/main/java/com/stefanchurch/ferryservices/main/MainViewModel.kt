package com.stefanchurch.ferryservices.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stefanchurch.ferryservices.API
import com.stefanchurch.ferryservices.models.Service
import kotlinx.coroutines.launch

class MainViewModel(private val api: API) : ViewModel() {
    val services: MutableLiveData<Array<Service>> by lazy {
        MutableLiveData<Array<Service>>()
    }

    init {
        viewModelScope.launch {
            services.value = api.getServices()
        }
    }
}