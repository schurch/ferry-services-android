package com.stefanchurch.ferryservices

import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class FerriesMessagingService: FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val installationID = InstallationID.getInstallationID(applicationContext)
        GlobalScope.launch {
            API.getInstance(applicationContext).updateInstallation(installationID, token)
        }
    }
}
