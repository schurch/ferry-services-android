package com.stefanchurch.ferryservicesandroid.ui.screens.details

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stefanchurch.ferryservicesandroid.BuildConfig
import com.stefanchurch.ferryservicesandroid.data.model.Service
import com.stefanchurch.ferryservicesandroid.data.model.ServiceStatus
import com.stefanchurch.ferryservicesandroid.data.repository.ServicesRepository
import com.stefanchurch.ferryservicesandroid.ui.model.ScheduledDepartureSectionUiModel
import com.stefanchurch.ferryservicesandroid.ui.model.globallySharedDepartureNote
import com.stefanchurch.ferryservicesandroid.ui.model.groupedDepartureSections
import com.stefanchurch.ferryservicesandroid.util.formatDate
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ServiceDetailsUiState(
    val loading: Boolean = true,
    val failedToLoad: Boolean = false,
    val service: Service? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val subscribed: Boolean = false,
    val loadingSubscribed: Boolean = false,
    val notificationsAuthorized: Boolean = false,
    val registeredForNotifications: Boolean = false,
    val scheduleSections: List<ScheduledDepartureSectionUiModel> = emptyList(),
    val sharedDepartureNote: String? = null,
    val errorMessage: String? = null,
) {
    val showSchedule: Boolean get() = service?.scheduledDeparturesAvailable == true
    val navigationTitle: String get() = service?.area ?: "Service"
    val selectedDateLabel: String get() = formatDate(selectedDate)
    val showScheduleWarning: Boolean get() = service?.status in setOf(
        ServiceStatus.DISRUPTED,
        ServiceStatus.CANCELLED,
        ServiceStatus.UNKNOWN,
    )
}

@HiltViewModel
class ServiceDetailsViewModel @Inject constructor(
    application: Application,
    private val repository: ServicesRepository,
) : AndroidViewModel(application) {
    private val service = MutableStateFlow<Service?>(null)
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val loading = MutableStateFlow(true)
    private val failedToLoad = MutableStateFlow(false)
    private val loadingSubscribed = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    private data class ServiceDetailsSnapshot(
        val service: Service?,
        val selectedDate: LocalDate,
        val subscribedIds: Set<Int>,
        val registeredForNotifications: Boolean,
        val loading: Boolean,
        val failedToLoad: Boolean,
        val loadingSubscribed: Boolean,
        val errorMessage: String?,
    )

    fun uiState(serviceId: Int): StateFlow<ServiceDetailsUiState> {
        val snapshotFlow = combine(
            combine(service, selectedDate, repository.subscribedServiceIds, repository.isRegisteredForNotifications) { currentService, date, subscribedIds, isRegistered ->
                PartialA(currentService, date, subscribedIds, isRegistered)
            },
            combine(loading, failedToLoad, loadingSubscribed, errorMessage) { isLoading, didFail, subscriptionLoading, message ->
                PartialB(isLoading, didFail, subscriptionLoading, message)
            },
        ) { a, b ->
            ServiceDetailsSnapshot(
                service = a.service,
                selectedDate = a.selectedDate,
                subscribedIds = a.subscribedIds,
                registeredForNotifications = a.registeredForNotifications,
                loading = b.loading,
                failedToLoad = b.failedToLoad,
                loadingSubscribed = b.loadingSubscribed,
                errorMessage = b.errorMessage,
            )
        }

        return snapshotFlow
            .map { snapshot ->
                ServiceDetailsUiState(
                    loading = snapshot.loading,
                    failedToLoad = snapshot.failedToLoad,
                    service = snapshot.service,
                    selectedDate = snapshot.selectedDate,
                    subscribed = serviceId in snapshot.subscribedIds,
                    loadingSubscribed = snapshot.loadingSubscribed,
                    notificationsAuthorized = NotificationManagerCompat.from(getApplication()).areNotificationsEnabled(),
                    registeredForNotifications = snapshot.registeredForNotifications,
                    scheduleSections = snapshot.service?.groupedDepartureSections(Instant.now()).orEmpty(),
                    sharedDepartureNote = snapshot.service?.globallySharedDepartureNote(),
                    errorMessage = snapshot.errorMessage,
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ServiceDetailsUiState())
    }

    private data class PartialA(
        val service: Service?,
        val selectedDate: LocalDate,
        val subscribedIds: Set<Int>,
        val registeredForNotifications: Boolean,
    )

    private data class PartialB(
        val loading: Boolean,
        val failedToLoad: Boolean,
        val loadingSubscribed: Boolean,
        val errorMessage: String?,
    )

    fun load(serviceId: Int) {
        if (service.value == null && loading.value) {
            viewModelScope.launch { refresh(serviceId) }
        }
    }

    fun refresh(serviceId: Int) {
        viewModelScope.launch {
            loading.value = true
            failedToLoad.value = false
            val fallback = repository.fetchDefaultServices().firstOrNull { it.serviceId == serviceId }
            if (service.value == null) service.value = fallback

            val date = selectedDate.value
            val resolved = runCatching { repository.fetchService(serviceId, date) }
                .recoverCatching { repository.fetchService(serviceId) }
                .recoverCatching {
                    repository.fetchServices().first { it.serviceId == serviceId }
                }
                .getOrElse { fallback }

            if (resolved != null) {
                service.value = resolved
            } else {
                failedToLoad.value = true
            }
            loading.value = false
        }
    }

    fun setSelectedDate(serviceId: Int, date: LocalDate) {
        selectedDate.value = date
        refresh(serviceId)
    }

    fun updateSubscribed(serviceId: Int, subscribed: Boolean) {
        viewModelScope.launch {
            loadingSubscribed.value = true
            errorMessage.value = null
            runCatching { repository.toggleSubscription(serviceId, subscribed) }
                .onFailure { errorMessage.value = "A problem occurred. Please try again later." }
            loadingSubscribed.value = false
        }
    }

    fun dismissError() {
        errorMessage.value = null
    }

    fun supportEmailIntent(serviceId: Int, state: ServiceDetailsUiState): Intent {
        val body = buildString {
            appendLine()
            appendLine("---")
            appendLine("Context")
            appendLine("- Service ID: $serviceId")
            appendLine("- Service area: ${state.service?.area ?: "Unknown"}")
            appendLine("- Service route: ${state.service?.route ?: "Unknown"}")
            appendLine("- Departures date selected: ${state.selectedDate}")
            appendLine("- Report generated at: ${Instant.now()}")
            appendLine("- App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("- Device OS: Android ${android.os.Build.VERSION.RELEASE}")
            appendLine("---")
        }
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${BuildConfig.SUPPORT_EMAIL}")
            putExtra(Intent.EXTRA_SUBJECT, "Timetable issue - Service $serviceId")
            putExtra(Intent.EXTRA_TEXT, body)
        }
    }
}
