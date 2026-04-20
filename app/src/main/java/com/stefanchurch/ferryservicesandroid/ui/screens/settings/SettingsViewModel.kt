package com.stefanchurch.ferryservicesandroid.ui.screens.settings

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stefanchurch.ferryservicesandroid.BuildConfig
import com.stefanchurch.ferryservicesandroid.data.repository.ServicesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val notificationsEnabledBySystem: Boolean = false,
    val serverPushEnabled: Boolean? = null,
    val versionText: String = "",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val repository: ServicesRepository,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            versionText = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
        ),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState

    fun refresh() {
        viewModelScope.launch {
            val systemEnabled = NotificationManagerCompat.from(getApplication()).areNotificationsEnabled()
            val serverEnabled = runCatching { repository.getPushStatus() }.getOrNull()
            _uiState.value = _uiState.value.copy(
                notificationsEnabledBySystem = systemEnabled,
                serverPushEnabled = serverEnabled,
            )
        }
    }

    fun setPushEnabled(enabled: Boolean) {
        viewModelScope.launch {
            runCatching { repository.updatePushStatus(enabled) }
            refresh()
        }
    }

    fun appNotificationSettingsIntent(): Intent {
        return Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, getApplication<Application>().packageName)
        }
    }

    fun supportEmailIntent(): Intent {
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${BuildConfig.SUPPORT_EMAIL}")
            putExtra(Intent.EXTRA_SUBJECT, "Scottish Ferries App (${BuildConfig.VERSION_NAME})")
        }
    }

    fun playStoreIntent(): Intent = Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.PLAY_STORE_URL))
}
