package com.stefanchurch.ferryservicesandroid.notifications

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.stefanchurch.ferryservicesandroid.util.hasAvailableGooglePlayServices
import kotlinx.coroutines.tasks.await

suspend fun fetchFcmToken(context: Context): String? {
    if (!hasAvailableGooglePlayServices(context)) return null

    return runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull()
}
