package com.example.trackstar.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val ModernDarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.Black,
    secondary = NeonGreen,
    background = DarkBackground,
    surface = LightSurface,
    onBackground = TextOnDark,
    onSurface = TextOnDark
)

@Composable
fun TrackStarMobileAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ModernDarkColorScheme,
        typography = AppTypography,
        content = content
    )
}


