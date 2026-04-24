package com.stefanchurch.ferryservicesandroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stefanchurch.ferryservicesandroid.data.repository.ServicesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ServicesRepository,
) : ViewModel() {
    private val _globalMessage = MutableStateFlow<String?>(null)
    val globalMessage: StateFlow<String?> = _globalMessage.asStateFlow()

    fun registerInstallation(deviceToken: String) {
        viewModelScope.launch {
            runCatching { repository.registerInstallation(deviceToken) }
        }
    }

    fun showAlert(message: String) {
        _globalMessage.value = message
    }

    fun clearAlert() {
        _globalMessage.value = null
    }
}
