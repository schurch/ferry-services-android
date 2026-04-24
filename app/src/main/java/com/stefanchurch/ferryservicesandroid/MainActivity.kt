package com.stefanchurch.ferryservicesandroid

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var launchIntent by mutableStateOf<Intent?>(null)
    private val viewModel: MainViewModel by viewModels()

    private val requestNotificationsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        registerInstallationIfPossible()
        launchIntent = intent
        setContent {
            FerryServicesAndroidApp(
                initialIntent = launchIntent,
                onIntentConsumed = { launchIntent = null },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        launchIntent = intent
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun registerInstallationIfPossible() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val token = task.result ?: return@addOnCompleteListener
            viewModel.registerInstallation(token)
        }
    }
}
