package com.stefanchurch.ferryservices.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Service(
    @SerialName("service_id") val serviceID: Int,
    val area: String,
    val route: String,
    @SerialName("status") val statusValue: Int,
    @SerialName("disruption_reason") val disruptionReason: String? = null,
    @SerialName("last_updated_date") val lastUpdatedDate: String? = null,
    val updated: String? = null,
    @SerialName("additional_info") val additionalInfo: String? = null,
    val locations: Array<Location>
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Service

        if (serviceID != other.serviceID) return false
        if (area != other.area) return false
        if (route != other.route) return false
        if (statusValue != other.statusValue) return false
        if (disruptionReason != other.disruptionReason) return false
        if (lastUpdatedDate != other.lastUpdatedDate) return false
        if (updated != other.updated) return false
        if (additionalInfo != other.additionalInfo) return false
        if (!locations.contentEquals(other.locations)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = serviceID
        result = 31 * result + area.hashCode()
        result = 31 * result + route.hashCode()
        result = 31 * result + statusValue
        result = 31 * result + (disruptionReason?.hashCode() ?: 0)
        result = 31 * result + (lastUpdatedDate?.hashCode() ?: 0)
        result = 31 * result + (updated?.hashCode() ?: 0)
        result = 31 * result + (additionalInfo?.hashCode() ?: 0)
        result = 31 * result + locations.contentHashCode()
        return result
    }
}

val Service.status: Status
    get() = Status.values().firstOrNull { it.value == statusValue } ?: Status.UNKNOWN
