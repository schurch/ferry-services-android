package com.stefanchurch.ferryservicesandroid.notifications

data class NotificationContent(
    val title: String,
    val body: String,
)

object NotificationContentResolver {
    fun resolve(
        payload: NotificationPayloadParser.Payload,
        appName: String,
        notificationTitle: String?,
        notificationBody: String?,
        dataTitle: String?,
        dataBody: String?,
    ): NotificationContent {
        val fallbackBody = when (payload) {
            is NotificationPayloadParser.Payload.Alert -> payload.message
            is NotificationPayloadParser.Payload.Service -> "Open service details"
        }

        // Preserve the legacy Android client contract: prefer display text carried in data.
        val title = dataTitle
            ?.takeIf(String::isNotBlank)
            ?: notificationTitle?.takeIf(String::isNotBlank)
            ?: appName

        val body = dataBody
            ?.takeIf(String::isNotBlank)
            ?: notificationBody?.takeIf(String::isNotBlank)
            ?: fallbackBody

        return NotificationContent(title = title, body = body)
    }
}
