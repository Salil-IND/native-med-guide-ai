package com.medguide.ai.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Medical Emergency Color Palette
val EmergencyRed = Color(0xFFD32F2F)
val EmergencyRedLight = Color(0xFFFF6659)
val EmergencyRedDark = Color(0xFF9A0007)
val MedBlue = Color(0xFF1565C0)
val MedBlueLight = Color(0xFF5E92F3)
val SafeGreen = Color(0xFF2E7D32)
val WarningOrange = Color(0xFFE65100)
val BackgroundDark = Color(0xFF0A0A0A)
val SurfaceDark = Color(0xFF1A1A1A)
val SurfaceVariantDark = Color(0xFF242424)
val OnSurfaceDark = Color(0xFFE8E8E8)

private val MedGuideDarkColorScheme = darkColorScheme(
    primary = EmergencyRed,
    onPrimary = Color.White,
    primaryContainer = EmergencyRedDark,
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = MedBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1A3A6E),
    onSecondaryContainer = Color(0xFFD6E3FF),
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFAAAAAA),
    error = Color(0xFFFF5252),
    outline = Color(0xFF444444)
)

private val MedGuideLightColorScheme = lightColorScheme(
    primary = EmergencyRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = EmergencyRedDark,
    secondary = MedBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6E3FF),
    onSecondaryContainer = Color(0xFF001945),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF666666),
    error = EmergencyRed,
    outline = Color(0xFFCCCCCC)
)

@Composable
fun MedGuideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MedGuideDarkColorScheme else MedGuideLightColorScheme
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
        typography = Typography(),
        content = content
    )
}