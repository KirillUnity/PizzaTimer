package com.example.clockplannerproject.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NightPrimary,
    onPrimary = NightSurface,
    secondary = Sage,
    tertiary = NightPrimary,
    background = NightSurface,
    surface = NightSurface,
    onBackground = NightInk,
    onSurface = NightInk,
    surfaceContainer = NightContainer,
)

private val LightColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = CreamOnPrimary,
    secondary = Sage,
    tertiary = Terracotta,
    background = Cream,
    surface = Cream,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = WarmGray,
    surfaceContainer = CreamContainer,
    surfaceContainerHigh = CreamContainer,
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
