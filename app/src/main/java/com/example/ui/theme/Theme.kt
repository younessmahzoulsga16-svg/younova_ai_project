package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val YounovaDarkColorScheme = darkColorScheme(
    primary = YounovaPrimaryPurple,
    onPrimary = YounovaPureBlack,
    primaryContainer = YounovaDeepPurple,
    onPrimaryContainer = YounovaBrightPurple,
    secondary = YounovaBrightPurple,
    onSecondary = YounovaPureBlack,
    secondaryContainer = YounovaSurfaceElevated,
    onSecondaryContainer = YounovaTextPrimary,
    tertiary = YounovaBrightPurple,
    background = YounovaPureBlack,
    onBackground = YounovaTextPrimary,
    surface = YounovaPureBlack,
    onSurface = YounovaTextPrimary,
    surfaceVariant = YounovaSurfaceDark,
    onSurfaceVariant = YounovaTextSecondary,
    outline = YounovaBorder,
    outlineVariant = YounovaBorderFocused,
    error = YounovaError,
    onError = YounovaTextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = YounovaPureBlack.toArgb()
                window.navigationBarColor = YounovaPureBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = YounovaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
