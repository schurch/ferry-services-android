package com.stefanchurch.ferryservices

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val lightColors = lightColors(
    primary = Color(0xFF000000),
    secondary = Color(0xFF858585),
    secondaryVariant = Color(0xFFE2E2E2),
    background = Color(0xFFFFFFFF),
)

private val darkColors = darkColors(
    primary = Color(0xFFFFFFFF),
    secondary = Color(0xFFB9B9B9),
    secondaryVariant = Color(0xFF575757),
    background = Color(0xFF000000)
)

val Colors.dullText: Color
    get() = if (isLight) Color(0xFFBBBBBB) else Color(0xFF888888)


@Composable
fun FerriesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) darkColors else lightColors,
        content = content
    )
}
