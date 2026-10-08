package com.example.clockplannerproject.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NightPrimary,
    onPrimary = NightCanvas,
    secondary = SandRest,
    onSecondary = PaperInk,
    tertiary = EmeraldWork,
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
    primaryContainer = TerracottaPressed,
    onPrimaryContainer = OnTerracotta,
    secondary = SandRest,
    onSecondary = PaperInk,
    secondaryContainer = PaperContainer,
    onSecondaryContainer = PaperInk,
    tertiary = EmeraldWork,
    onTertiary = OnTerracotta,
    background = PaperCanvas,
    surface = PaperSurface,
    onBackground = PaperInk,
    onSurface = PaperInk,
    onSurfaceVariant = PaperInkMuted,
    surfaceContainerLowest = OnTerracotta,
    surfaceContainerLow = PaperContainerLow,
    surfaceContainer = PaperContainer,
    surfaceContainerHigh = PaperContainerHigh,
    surfaceContainerHighest = PaperContainerHigh,
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
