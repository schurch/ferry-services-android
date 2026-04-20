package com.stefanchurch.ferryservicesandroid.ui.screens.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stefanchurch.ferryservicesandroid.data.model.Service
import com.stefanchurch.ferryservicesandroid.data.repository.ServicesRepository
import com.stefanchurch.ferryservicesandroid.ui.model.ServiceRowUiModel
import com.stefanchurch.ferryservicesandroid.ui.model.ServicesSectionUiModel
import com.stefanchurch.ferryservicesandroid.ui.model.operatorLogoRes
import com.stefanchurch.ferryservicesandroid.ui.model.toRowUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ServicesUiState(
    val loading: Boolean = true,
    val searchText: String = "",
    val sections: List<ServicesSectionUiModel> = emptyList(),
)

@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val repository: ServicesRepository,
) : ViewModel() {
    private val allServices = MutableStateFlow<List<Service>>(emptyList())
    private val searchText = MutableStateFlow("")
    private val loading = MutableStateFlow(true)

    val uiState: StateFlow<ServicesUiState> = combine(
        allServices,
        repository.subscribedServiceIds,
        searchText,
        loading,
    ) { services, subscribedIds, query, isLoading ->
        ServicesUiState(
            loading = isLoading,
            searchText = query,
            sections = buildSections(services, subscribedIds, query),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ServicesUiState())

    init {
        refresh()
    }

    fun updateSearchText(value: String) {
        searchText.value = value
    }

    fun refresh() {
        viewModelScope.launch {
            loading.value = true
            val fallback = repository.fetchDefaultServices()
            allServices.value = fallback
            runCatching { repository.fetchServices() }
                .onSuccess { allServices.value = it }
            loading.value = false
        }
    }

    private fun buildSections(
        services: List<Service>,
        subscribedIds: Set<Int>,
        query: String,
    ): List<ServicesSectionUiModel> {
        if (query.isNotBlank()) {
            val lowered = query.trim().lowercase()
            return listOf(
                ServicesSectionUiModel(
                    title = "Results",
                    subscribed = false,
                    rows = services.filter {
                        it.area.lowercase().contains(lowered) || it.route.lowercase().contains(lowered)
                    }.map(Service::toRowUiModel),
                ),
            )
        }

        val subscribedRows = services.filter { it.serviceId in subscribedIds }.map(Service::toRowUiModel)
        val operatorSections = services.groupBy { it.serviceOperator?.id ?: 0 }
            .values
            .sortedBy { it.firstOrNull()?.serviceOperator?.name.orEmpty() }
            .map { operatorServices ->
                val representative = operatorServices.first()
                ServicesSectionUiModel(
                    title = representative.serviceOperator?.name ?: "Services",
                    subscribed = false,
                    imageRes = representative.operatorLogoRes(),
                    rows = operatorServices.map(Service::toRowUiModel),
                )
            }

        return buildList {
            if (subscribedRows.isNotEmpty()) {
                add(
                    ServicesSectionUiModel(
                        title = "Subscribed",
                        subscribed = true,
                        rows = subscribedRows,
                    ),
                )
            }
            addAll(operatorSections)
        }
    }
}
