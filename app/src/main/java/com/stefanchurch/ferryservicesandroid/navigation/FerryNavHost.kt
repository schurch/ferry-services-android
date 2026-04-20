package com.stefanchurch.ferryservicesandroid.navigation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stefanchurch.ferryservicesandroid.ui.FerryAppState
import com.stefanchurch.ferryservicesandroid.ui.screens.details.ServiceDetailsScreen
import com.stefanchurch.ferryservicesandroid.ui.screens.map.ServiceMapScreen
import com.stefanchurch.ferryservicesandroid.ui.screens.services.ServicesScreen
import com.stefanchurch.ferryservicesandroid.ui.screens.settings.SettingsScreen
import com.stefanchurch.ferryservicesandroid.ui.screens.webinfo.WebInfoScreen
import kotlinx.coroutines.flow.StateFlow

@Composable
fun FerryNavHost(
    appState: FerryAppState,
    globalMessage: StateFlow<String?>,
    onMessageDismissed: () -> Unit,
) {
    val navController = rememberNavController()
    val message by globalMessage.collectAsState()

    appState.bind(navController)

    NavHost(
        navController = navController,
        startDestination = Routes.Services.route,
    ) {
        composable(Routes.Services.route) {
            ServicesScreen(
                openSettings = { navController.navigate(Routes.Settings.route) },
                openService = { navController.navigate(Routes.serviceDetails(it)) },
            )
        }
        composable(
            route = Routes.ServiceDetails.route,
            arguments = listOf(navArgument("serviceId") { type = NavType.IntType }),
        ) { backStackEntry ->
            ServiceDetailsScreen(
                serviceId = backStackEntry.arguments?.getInt("serviceId") ?: return@composable,
                onBack = navController::popBackStack,
                openMap = { navController.navigate(Routes.serviceMap(it)) },
                openWebInfo = { html -> navController.navigate(Routes.webInfo(html)) },
            )
        }
        composable(
            route = Routes.Map.route,
            arguments = listOf(navArgument("serviceId") { type = NavType.IntType }),
        ) { backStackEntry ->
            ServiceMapScreen(
                serviceId = backStackEntry.arguments?.getInt("serviceId") ?: return@composable,
                onBack = navController::popBackStack,
            )
        }
        composable(
            route = Routes.WebInfo.route,
            arguments = listOf(navArgument("html") { type = NavType.StringType }),
        ) { backStackEntry ->
            WebInfoScreen(
                html = backStackEntry.arguments?.getString("html").orEmpty(),
                onBack = navController::popBackStack,
            )
        }
        composable(Routes.Settings.route) {
            SettingsScreen(onBack = navController::popBackStack)
        }
    }

    if (message != null) {
        AlertDialog(
            onDismissRequest = onMessageDismissed,
            confirmButton = {
                TextButton(onClick = onMessageDismissed) {
                    Text("OK")
                }
            },
            title = { Text("Alert") },
            text = { Text(message.orEmpty()) },
        )
    }
}

sealed class Routes(val route: String) {
    data object Services : Routes("services")
    data object Settings : Routes("settings")
    data object ServiceDetails : Routes("service/{serviceId}")
    data object Map : Routes("map/{serviceId}")
    data object WebInfo : Routes("webinfo/{html}")

    companion object {
        fun serviceDetails(serviceId: Int) = "service/$serviceId"
        fun serviceMap(serviceId: Int) = "map/$serviceId"
        fun webInfo(html: String) = "webinfo/${android.net.Uri.encode(html)}"
    }
}
