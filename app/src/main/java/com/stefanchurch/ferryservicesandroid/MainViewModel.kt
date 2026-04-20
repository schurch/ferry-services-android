package com.stefanchurch.ferryservicesandroid

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _globalMessage = MutableStateFlow<String?>(null)
    val globalMessage: StateFlow<String?> = _globalMessage.asStateFlow()

    fun showAlert(message: String) {
        _globalMessage.value = message
    }

    fun clearAlert() {
        _globalMessage.value = null
    }
}
