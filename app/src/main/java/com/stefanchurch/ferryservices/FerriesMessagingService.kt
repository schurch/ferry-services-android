package com.stefanchurch.ferryservices

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.remoteMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FerriesMessagingService: FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val installationID = InstallationID.getInstallationID(applicationContext)

        GlobalScope.launch {
            ServicesRepository
                .getInstance(applicationContext)
                .updateInstallation(installationID, token)

            val prefs = applicationContext.getSharedPreferences(applicationContext.getString(R.string.preferences_key), MODE_PRIVATE)
            with(prefs.edit()) {
                putBoolean(applicationContext.getString(R.string.preferences_created_installation_key), true)
                apply()
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data.isEmpty()) { return }

        val title = message.data["title"] ?: return
        val body = message.data["body"] ?: return
        val serviceId = message.data["service_id"] ?: return

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("service_id", serviceId)
        intent.action = System.currentTimeMillis().toString()

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = "FERRIES_SERVICE_STATUS_UPDATES"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
            .setVisibility(VISIBILITY_PUBLIC)

        val notificationChannel = NotificationChannel(
            channelId,
            "Service status updates",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationChannel.enableVibration(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
        notificationManager.notify(SystemClock.uptimeMillis().toInt(), notificationBuilder.build())
    }
}
