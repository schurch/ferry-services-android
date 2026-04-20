package com.stefanchurch.ferryservicesandroid.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stefanchurch.ferryservicesandroid.data.model.Service
import com.stefanchurch.ferryservicesandroid.data.repository.ServicesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ServiceMapViewModel @Inject constructor(
    private val repository: ServicesRepository,
) : ViewModel() {
    private val _service = MutableStateFlow<Service?>(null)
    val service: StateFlow<Service?> = _service

    fun load(serviceId: Int) {
        if (_service.value != null) return
        viewModelScope.launch {
            _service.value = runCatching { repository.fetchService(serviceId) }
                .getOrElse { repository.fetchDefaultServices().firstOrNull { it.serviceId == serviceId } }
        }
    }
}
