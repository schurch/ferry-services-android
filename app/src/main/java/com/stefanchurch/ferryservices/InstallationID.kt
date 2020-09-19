package com.stefanchurch.ferryservices

import android.content.Context
import java.util.*

class InstallationID {
    companion object {
        fun getInstallationID(context: Context) : UUID {
            val sharedPreferences =  context.getSharedPreferences(context.getString(R.string.preferences_key), Context.MODE_PRIVATE)

            val stringUUID = sharedPreferences.getString(context.getString(R.string.preferences_installation_id_key), null)  ?: with (sharedPreferences.edit()) {
                val newInstallationID = UUID.randomUUID().toString()
                putString(context.getString(R.string.preferences_installation_id_key), newInstallationID)
                apply()
                newInstallationID
            }

            return UUID.fromString(stringUUID)
        }
    }
}