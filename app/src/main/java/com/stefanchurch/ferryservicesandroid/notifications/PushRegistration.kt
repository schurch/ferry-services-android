package com.stefanchurch.ferryservicesandroid.notifications

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

suspend fun fetchFcmToken(): String = FirebaseMessaging.getInstance().token.await()
