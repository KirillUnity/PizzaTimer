package com.example.clockplannerproject.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NightPrimary,
    onPrimary = NightCanvas,
    secondary = Color(0xFFD2C4B7),
    onSecondary = NightCanvas,
    secondaryContainer = NightContainer,
    onSecondaryContainer = NightInk,
    tertiary = EmeraldWork,
    onTertiary = NightCanvas,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = NightCanvas,
    surface = NightCanvas,
    onBackground = NightInk,
    onSurface = NightInk,
    onSurfaceVariant = NightInk.copy(alpha = 0.72f),
    surfaceContainer = NightContainer,
    surfaceContainerLow = NightContainerLow,
    surfaceContainerHigh = NightContainerHigh,
    outline = CompletedWarmGray,
    outlineVariant = NightContainer,
)

private val LightColorScheme = lightColorScheme(
    primary = TerracottaNow,
    onPrimary = OnTerracotta,
    primaryContainer = Color(0xFFCD4630),
    onPrimaryContainer = OnTerracotta,
    secondary = PaperSecondary,
    onSecondary = OnTerracotta,
    secondaryContainer = SandRest,
    onSecondaryContainer = SandInk,
    tertiary = EmeraldWork,
    onTertiary = OnTerracotta,
    tertiaryContainer = Color(0xFFA4F4BF),
    onTertiaryContainer = Color(0xFF002110),
    error = Color(0xFFBA1A1A),
    onError = OnTerracotta,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = PaperCanvas,
    surface = PaperSurface,
    onBackground = PaperInk,
    onSurface = PaperInk,
    onSurfaceVariant = PaperInkMuted,
    surfaceContainerLowest = OnTerracotta,
    surfaceContainerLow = PaperContainerLow,
    surfaceContainer = PaperContainer,
    surfaceContainerHigh = PaperContainerHigh,
    surfaceContainerHighest = Color(0xFFE6E2DD),
    outline = PaperOutline,
    outlineVariant = SandBorder,
)

@Composable
fun ClockPlannerProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
