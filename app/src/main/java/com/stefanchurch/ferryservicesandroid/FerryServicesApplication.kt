package com.stefanchurch.ferryservicesandroid

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.gms.maps.MapsInitializer
import com.stefanchurch.ferryservicesandroid.util.hasAvailableGooglePlayServices
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FerryServicesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().isAutoInitEnabled = hasAvailableGooglePlayServices(this)
        runCatching { MapsInitializer.initialize(this) }
    }
}
