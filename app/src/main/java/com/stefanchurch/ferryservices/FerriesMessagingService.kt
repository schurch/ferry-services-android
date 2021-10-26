package com.stefanchurch.ferryservices

import com.google.firebase.messaging.FirebaseMessagingService
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
}
