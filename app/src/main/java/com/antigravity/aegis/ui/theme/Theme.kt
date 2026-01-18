package com.antigravity.aegis.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PremiumSecurityValidColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = NavyBlue,
    primaryContainer = GoldDim,
    onPrimaryContainer = NavyBlue,
    secondary = Gold,
    onSecondary = NavyBlue,
    secondaryContainer = GoldDim,
    onSecondaryContainer = NavyBlue,
    background = NavyBlue,
    onBackground = LightGray,
    surface = NavyBlue,
    onSurface = LightGray,
    surfaceVariant = NavyBlueDark,
    onSurfaceVariant = LightGray,
    outline = Gold,
    error = Error,
    onError = OnError
)

private val PremiumSecurityLightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = NavyBlue,
    onPrimary = Gold,
    primaryContainer = NavyBlueDark,
    onPrimaryContainer = Gold,
    secondary = Gold,
    onSecondary = NavyBlue,
    secondaryContainer = GoldDim,
    onSecondaryContainer = NavyBlue,
    background = Color(0xFFF5F5F7),
    onBackground = NavyBlue,
    surface = Color.White,
    onSurface = NavyBlue,
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = NavyBlue,
    outline = NavyBlue,
    error = Error,
    onError = Color.White
)

@Composable
fun AegisTheme(
    darkTheme: Boolean = true, // Default to true, but controllable
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) PremiumSecurityValidColorScheme else PremiumSecurityLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
