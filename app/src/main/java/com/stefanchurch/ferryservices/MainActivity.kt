package com.stefanchurch.ferryservices

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.stefanchurch.ferryservices.databinding.MainActivityBinding
import com.stefanchurch.ferryservices.detail.ServiceDetailArgument
import com.stefanchurch.ferryservices.main.MainFragmentDirections
import com.stefanchurch.ferryservices.models.Location
import com.stefanchurch.ferryservices.models.Service
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView<MainActivityBinding>(this, R.layout.main_activity)

        val navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupWithNavController(
            findViewById(R.id.toolbar),
            navController,
            appBarConfiguration
        )

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                task.result?.token?.let {
                    GlobalScope.launch {
                        try {
                            val installationID =
                                InstallationID.getInstallationID(applicationContext)

                            ServicesRepository
                                .getInstance(applicationContext)
                                .updateInstallation(installationID, it)

                            val prefs = applicationContext.getSharedPreferences(
                                applicationContext.getString(R.string.preferences_key),
                                MODE_PRIVATE
                            )

                            with(prefs.edit()) {
                                putBoolean(
                                    applicationContext.getString(R.string.preferences_created_installation_key),
                                    true
                                )
                                apply()
                            }
                        } catch (exception: Throwable) {
                            // Ignore any errors and let the application continue
                        }
                    }
                }
            })

        intent.extras?.getString("service_id")?.let {
            val direction = MainFragmentDirections.actionMainFragmentToServiceDetail(
                ServiceDetailArgument(it.toInt(), null)
            )
            navController.navigate(direction)
        }
    }

}