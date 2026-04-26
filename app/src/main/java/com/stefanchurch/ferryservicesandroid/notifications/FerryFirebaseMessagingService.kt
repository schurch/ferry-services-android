package com.stefanchurch.ferryservicesandroid.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.stefanchurch.ferryservicesandroid.MainActivity
import com.stefanchurch.ferryservicesandroid.R
import com.stefanchurch.ferryservicesandroid.data.repository.ServicesRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FerryFirebaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var servicesRepository: ServicesRepository

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { servicesRepository.registerInstallation(token) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val payload = NotificationPayloadParser.fromData(message.data) ?: return
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            NotificationPayloadParser.applyToIntent(this, payload)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            payload.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val content = NotificationContentResolver.resolve(
            payload = payload,
            appName = getString(R.string.app_name),
            notificationTitle = message.notification?.title,
            notificationBody = message.notification?.body,
            dataTitle = message.data["title"],
            dataBody = message.data["body"],
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_ferry)
            .setContentTitle(content.title)
            .setContentText(content.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(payload.hashCode(), notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Service Alerts",
            NotificationManager.IMPORTANCE_HIGH,
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "service-alerts"
    }
}
