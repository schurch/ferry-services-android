package com.stefanchurch.ferryservicesandroid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = FerryTint,
    secondary = FerryGreen,
    tertiary = FerryAmber,
    error = FerryRed,
    surface = Color.White,
    background = Color.White,
    surfaceVariant = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = FerryTint,
    secondary = FerryGreen,
    tertiary = FerryAmber,
    error = FerryRed,
    surface = Color.White,
    background = Color.White,
    surfaceVariant = Color.White,
)

@Composable
fun FerryServicesTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
