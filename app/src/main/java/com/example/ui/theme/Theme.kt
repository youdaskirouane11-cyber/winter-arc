package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = AthleteDarkBg,
    primaryContainer = AthleteDarkCard,
    onPrimaryContainer = GoldPrimary,
    secondary = SpeedBlue,
    onSecondary = AthleteDarkBg,
    tertiary = VoltGreen,
    onTertiary = AthleteDarkBg,
    background = AthleteDarkBg,
    onBackground = TextPrimaryDark,
    surface = AthleteDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = AthleteDarkCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = AthleteDarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = GoldSecondary,
    onPrimary = AthleteLightBg,
    primaryContainer = AthleteLightCard,
    onPrimaryContainer = GoldSecondary,
    secondary = SpeedBlue,
    onSecondary = AthleteLightBg,
    tertiary = VoltGreen,
    onTertiary = AthleteLightBg,
    background = AthleteLightBg,
    onBackground = TextPrimaryLight,
    surface = AthleteLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = AthleteLightCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = AthleteLightCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Dark mode as requested
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
