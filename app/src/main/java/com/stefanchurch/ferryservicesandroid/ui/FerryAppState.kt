package com.stefanchurch.ferryservicesandroid.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.stefanchurch.ferryservicesandroid.navigation.Routes

class FerryAppState {
    private var navController: NavHostController? = null

    fun bind(controller: NavHostController) {
        navController = controller
    }

    fun openService(serviceId: Int) {
        navController?.navigate(Routes.serviceDetails(serviceId))
    }
}

@Composable
fun rememberFerryAppState(): FerryAppState = remember { FerryAppState() }
