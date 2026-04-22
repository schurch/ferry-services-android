package com.stefanchurch.ferryservicesandroid.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = FerryTint,
    onPrimary = FerryOnTint,
    primaryContainer = FerryTintContainer,
    onPrimaryContainer = FerryOnTintContainer,
    secondary = FerryGreen,
    tertiary = FerryAmber,
    error = FerryRed,
    background = FerrySea,
    onBackground = Color(0xFF171D1B),
    surface = Color.White,
    onSurface = Color(0xFF171D1B),
    surfaceVariant = FerrySeaVariant,
    onSurfaceVariant = Color(0xFF3F4946),
)

private val DarkColors = darkColorScheme(
    primary = FerryTintDark,
    onPrimary = FerryOnTint,
    primaryContainer = Color(0xFF005047),
    onPrimaryContainer = FerryTintContainer,
    secondary = FerryGreenDark,
    tertiary = FerryAmberDark,
    error = FerryRedDark,
    background = FerryNightSea,
    onBackground = Color(0xFFDDE5E2),
    surface = FerryNightSurface,
    onSurface = Color(0xFFDDE5E2),
    surfaceVariant = FerryNightSurfaceVariant,
    onSurfaceVariant = Color(0xFFBFC9C5),
)

@Composable
fun FerryServicesTheme(
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
