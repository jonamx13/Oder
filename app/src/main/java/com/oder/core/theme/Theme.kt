package com.oder.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Strictly Dark Mode color scheme (OLED Blacks to #1E1E1E)
private val StrictlyDarkColorScheme = darkColorScheme(
    primary = TextPrimary,
    onPrimary = OledBlack,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = TextPrimary,
    secondary = TextSecondary,
    onSecondary = OledBlack,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = NounMasculine,
    onTertiary = OledBlack,
    background = OledBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceTint = OledBlack,
    outline = DarkBorder,
    outlineVariant = DarkSurfaceElevated,
    error = AccentError,
    onError = OledBlack
)

@Composable
fun OderTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = StrictlyDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // Dark appearance for system bars (light icons on dark background)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    CompositionLocalProvider(
        LocalGenderColors provides GenderColors()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = OderTypography,
            content = content
        )
    }
}

// Convenient accessor for custom gender colors
object OderTheme {
    val genderColors: GenderColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGenderColors.current
}
