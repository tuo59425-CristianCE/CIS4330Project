package com.example.trackstar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

// Define your app's colors here
val primaryColor = Color(0xFF6200EE)
val secondaryColor = Color(0xFF03DAC5)

@Composable
fun TrackStarMobileAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Material3 Theme Setup
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            secondary = secondaryColor
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            secondary = secondaryColor
        )
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TrackStarMobileAppTheme {
        // Your preview UI here
    }
}
