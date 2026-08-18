package com.oder.core.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// OLED Black & Dark Mode Palette (#000000 to #1E1E1E)
val OledBlack = Color(0xFF000000)
val DarkSurface = Color(0xFF121212)
val DarkSurfaceVariant = Color(0xFF1E1E1E)
val DarkSurfaceElevated = Color(0xFF252525)
val DarkBorder = Color(0xFF2C2C2E)

// Text Colors
val TextPrimary = Color(0xFFF5F5F7)
val TextSecondary = Color(0xFFA1A1A6)
val TextTertiary = Color(0xFF636366)

// Semantic Noun Gender Colors (Muted & Premium)
val NounMasculine = Color(0xFF4A90E2) // MASCULINE_BLUE (der / męski)
val NounFeminine = Color(0xFFE05666)  // FEMININE_RED (die / żeński)
val NounNeuter = Color(0xFF48BB78)    // NEUTER_GREEN (das / nijaki)
val NounPlural = Color(0xFFECC94B)    // Plural / Mnoga

// Semantic Feedback Colors
val AccentSuccess = Color(0xFF34C759)
val AccentWarning = Color(0xFFFF9F0A)
val AccentError = Color(0xFFFF453A)

// Data class to inject gender colors via CompositionLocal
data class GenderColors(
    val masculine: Color = NounMasculine,
    val feminine: Color = NounFeminine,
    val neuter: Color = NounNeuter,
    val plural: Color = NounPlural
)

val LocalGenderColors = staticCompositionLocalOf { GenderColors() }
