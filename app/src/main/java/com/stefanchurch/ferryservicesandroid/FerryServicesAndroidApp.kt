package com.stefanchurch.ferryservicesandroid

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.stefanchurch.ferryservicesandroid.navigation.FerryNavHost
import com.stefanchurch.ferryservicesandroid.notifications.NotificationPayloadParser
import com.stefanchurch.ferryservicesandroid.ui.FerryAppState
import com.stefanchurch.ferryservicesandroid.ui.rememberFerryAppState
import com.stefanchurch.ferryservicesandroid.ui.theme.FerryServicesTheme

@Composable
fun FerryServicesAndroidApp(
    initialIntent: Intent?,
    onIntentConsumed: () -> Unit,
    appState: FerryAppState = rememberFerryAppState(),
) {
    val appViewModel: MainViewModel = hiltViewModel()

    LaunchedEffect(initialIntent) {
        if (initialIntent != null) {
            when (val payload = NotificationPayloadParser.fromIntent(initialIntent)) {
                is NotificationPayloadParser.Payload.Alert -> appViewModel.showAlert(payload.message)
                is NotificationPayloadParser.Payload.Service -> appState.openService(payload.serviceId)
                null -> Unit
            }
            onIntentConsumed()
        }
    }

    FerryServicesTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            FerryNavHost(
                appState = appState,
                globalMessage = appViewModel.globalMessage,
                onMessageDismissed = appViewModel::clearAlert,
            )
        }
    }
}
