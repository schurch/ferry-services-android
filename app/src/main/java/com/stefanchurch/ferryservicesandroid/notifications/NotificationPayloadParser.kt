package com.stefanchurch.ferryservicesandroid.notifications

import android.content.Intent

object NotificationPayloadParser {
    sealed interface Payload {
        data class Service(val serviceId: Int) : Payload
        data class Alert(val message: String) : Payload
    }

    private const val EXTRA_SERVICE_ID = "extra_service_id"
    private const val EXTRA_ALERT_MESSAGE = "extra_alert_message"

    fun fromData(data: Map<String, String>): Payload? {
        data["service_id"]?.toIntOrNull()?.let { return Payload.Service(it) }
        data["alert"]?.takeIf(String::isNotBlank)?.let { return Payload.Alert(it) }
        return null
    }

    fun fromIntent(intent: Intent): Payload? {
        intent.getStringExtra(EXTRA_ALERT_MESSAGE)?.let { return Payload.Alert(it) }
        intent.getIntExtra(EXTRA_SERVICE_ID, -1).takeIf { it > 0 }?.let { return Payload.Service(it) }
        return null
    }

    fun applyToIntent(intent: Intent, payload: Payload) {
        when (payload) {
            is Payload.Alert -> intent.putExtra(EXTRA_ALERT_MESSAGE, payload.message)
            is Payload.Service -> intent.putExtra(EXTRA_SERVICE_ID, payload.serviceId)
        }
    }
}
