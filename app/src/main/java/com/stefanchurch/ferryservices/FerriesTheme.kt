package com.stefanchurch.ferryservices

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColors(
    primary = Color(0xFF000000),
    secondary = Color(0xFF858585),
    secondaryVariant = Color(0xFFE2E2E2)
)

private val DarkColors = darkColors(
    primary = Color(0xFFFFFFFF),
    secondary = Color(0xFFB9B9B9),
    secondaryVariant = Color(0xFF575757)
)

@Composable
fun FerriesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
