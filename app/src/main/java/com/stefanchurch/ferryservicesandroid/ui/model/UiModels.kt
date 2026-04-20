package com.stefanchurch.ferryservicesandroid.ui.model

import androidx.annotation.DrawableRes
import com.stefanchurch.ferryservicesandroid.R
import com.stefanchurch.ferryservicesandroid.data.model.Service
import com.stefanchurch.ferryservicesandroid.data.model.ServiceStatus
import com.stefanchurch.ferryservicesandroid.util.formatTime
import com.stefanchurch.ferryservicesandroid.util.parseInstant
import java.time.Instant

data class ServicesSectionUiModel(
    val title: String,
    val subscribed: Boolean,
    @DrawableRes val imageRes: Int? = null,
    val rows: List<ServiceRowUiModel>,
)

data class ServiceRowUiModel(
    val service: Service,
    val area: String,
    val route: String,
    val disruptionText: String,
    val status: ServiceStatus,
)

data class ScheduledDepartureSectionUiModel(
    val originName: String,
    val destinationName: String,
    val sharedNote: String?,
    val rows: List<ScheduledDepartureRowUiModel>,
)

data class ScheduledDepartureRowUiModel(
    val departureTimeText: String,
    val arrivalTimeText: String,
    val note: String?,
    val isPastDeparture: Boolean,
)

fun Service.toRowUiModel(): ServiceRowUiModel {
    return ServiceRowUiModel(
        service = this,
        area = area,
        route = route,
        disruptionText = when (status) {
            ServiceStatus.NORMAL -> "Normal Operations"
            ServiceStatus.DISRUPTED -> "Sailings Disrupted"
            ServiceStatus.CANCELLED -> "Sailings Cancelled"
            ServiceStatus.UNKNOWN -> "Unknown Status"
        },
        status = status,
    )
}

fun Service.operatorLogoRes(): Int? = when (serviceOperator?.id) {
    1 -> R.drawable.calmac_logo
    else -> null
}

fun Service.groupedDepartureSections(selectedInstant: Instant = Instant.now()): List<ScheduledDepartureSectionUiModel> {
    val globalSharedNote = globallySharedDepartureNote()
    return locations
        .sortedBy { it.name.lowercase() }
        .flatMap { location ->
            location.scheduledDepartures
                .orEmpty()
                .groupBy { it.destination.id }
                .values
                .sortedBy { parseInstant(it.firstOrNull()?.departure) ?: Instant.EPOCH }
                .map { departures ->
                    val normalizedNotes = departures.map { it.note?.trim()?.takeIf(String::isNotBlank) }
                    val firstNote = normalizedNotes.firstOrNull()
                    val sectionSharedNote = firstNote?.takeIf { normalizedNotes.all { note -> note == firstNote } }
                    ScheduledDepartureSectionUiModel(
                        originName = location.name,
                        destinationName = departures.firstOrNull()?.destination?.name.orEmpty(),
                        sharedNote = if (globalSharedNote == null) sectionSharedNote else null,
                        rows = departures.map { departure ->
                            val departureInstant = parseInstant(departure.departure)
                            ScheduledDepartureRowUiModel(
                                departureTimeText = formatTime(departure.departure),
                                arrivalTimeText = formatTime(departure.arrival),
                                note = if (globalSharedNote == null && sectionSharedNote == null) {
                                    departure.note?.trim()?.takeIf(String::isNotBlank)
                                } else {
                                    null
                                },
                                isPastDeparture = departureInstant != null && departureInstant <= selectedInstant,
                            )
                        },
                    )
                }
        }
}

fun Service.globallySharedDepartureNote(): String? {
    val notes = locations.flatMap { it.scheduledDepartures.orEmpty() }
        .mapNotNull { it.note?.trim()?.takeIf(String::isNotBlank) }
    val first = notes.firstOrNull() ?: return null
    return first.takeIf { notes.all { note -> note == first } }
}
