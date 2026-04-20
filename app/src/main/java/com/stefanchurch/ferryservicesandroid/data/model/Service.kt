package com.stefanchurch.ferryservicesandroid.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Service(
    @SerialName("service_id") val serviceId: Int,
    val status: ServiceStatus = ServiceStatus.UNKNOWN,
    val area: String,
    val route: String,
    @SerialName("disruption_reason") val disruptionReason: String? = null,
    @SerialName("last_updated_date") val lastUpdatedDate: String? = null,
    val updated: String? = null,
    @SerialName("additional_info") val additionalInfo: String? = null,
    val locations: List<Location>,
    val vessels: List<Vessel> = emptyList(),
    @SerialName("operator") val serviceOperator: ServiceOperator? = null,
    @SerialName("scheduled_departures_available") val scheduledDeparturesAvailable: Boolean? = null,
) {
    @Serializable
    data class Location(
        val id: Int,
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val weather: Weather? = null,
        @SerialName("scheduled_departures") val scheduledDepartures: List<ScheduledDeparture>? = null,
        @SerialName("next_departure") val nextDeparture: ScheduledDeparture? = null,
        @SerialName("next_rail_departure") val nextRailDeparture: RailDeparture? = null,
    ) {
        @Serializable
        data class Weather(
            val description: String,
            val icon: String,
            @SerialName("temperature_celsius") val temperatureCelsius: Int,
            @SerialName("wind_speed_mph") val windSpeedMph: Int,
            @SerialName("wind_direction") val windDirection: Int,
            @SerialName("wind_direction_cardinal") val windDirectionCardinal: String,
        )

        @Serializable
        data class ScheduledDeparture(
            val departure: String,
            val arrival: String,
            val destination: DepartureLocation,
            @SerialName("notes") val note: String? = null,
        ) {
            @Serializable
            data class DepartureLocation(
                val id: Int,
                val name: String,
                val latitude: Double,
                val longitude: Double,
            )
        }

        @Serializable
        data class RailDeparture(
            val from: String,
            val to: String,
            val departure: String,
            @SerialName("departure_info") val departureInfo: String,
            @SerialName("is_cancelled") val isCancelled: Boolean,
            val platform: String? = null,
        )
    }

    @Serializable
    data class ServiceOperator(
        val id: Int,
        val name: String,
        val website: String? = null,
        @SerialName("local_number") val localNumber: String? = null,
        @SerialName("international_number") val internationalNumber: String? = null,
        val email: String? = null,
        val x: String? = null,
        val facebook: String? = null,
    )
}

@Serializable(with = ServiceStatusSerializer::class)
enum class ServiceStatus {
    NORMAL,
    DISRUPTED,
    CANCELLED,
    UNKNOWN,
}
