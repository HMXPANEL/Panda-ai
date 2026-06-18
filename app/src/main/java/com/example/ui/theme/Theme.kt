package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PandaDarkScheme = darkColorScheme(
    primary           = GradientStart,
    onPrimary         = Color.White,
    primaryContainer  = GradientEnd,
    secondary         = CyanGlow,
    onSecondary       = Color.Black,
    tertiary          = PurpleAccent,
    background        = DeepSpace,
    onBackground      = TextPrimary,
    surface           = NavyDark,
    onSurface         = TextPrimary,
    surfaceVariant    = NavyMid,
    onSurfaceVariant  = TextSecondary,
    outline           = GlassBorder,
    error             = StatusRed,
    onError           = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // We force dark theme because Liquid Glass aesthetic is dark-first
    dynamicColor: Boolean = false, // We use our premium bespoke colors for glassmorphism
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = PandaDarkScheme,
        typography  = Typography,
        content     = content
    )
}
