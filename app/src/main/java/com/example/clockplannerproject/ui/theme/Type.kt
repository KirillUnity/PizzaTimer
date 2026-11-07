@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.example.clockplannerproject.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.clockplannerproject.R

/**
 * Domine: dial numerals and editorial headlines.
 * Plus Jakarta Sans: operational UI.
 * Variable files need the `wght` axis or every face draws at the default weight.
 * Letter-spacing follows DESIGN.md `em` values, converted to `sp` at each size.
 *
 * @since 0.5.0
 */
private fun variableFace(resId: Int, weight: FontWeight): Font = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(weight, FontStyle.Normal),
)

/** DESIGN.md stores tracking in em. Compose letter-spacing is an absolute size. */
private fun emTracking(fontSizeSp: Int, em: Float): TextUnit = (fontSizeSp * em).sp

private const val TabularFigures = "tnum"

val DomineFamily = FontFamily(
    variableFace(R.font.domine_wght, FontWeight.Normal),
    variableFace(R.font.domine_wght, FontWeight.SemiBold),
    variableFace(R.font.domine_wght, FontWeight.Bold),
)

val PlusJakartaFamily = FontFamily(
    variableFace(R.font.plus_jakarta_sans_wght, FontWeight.Normal),
    variableFace(R.font.plus_jakarta_sans_wght, FontWeight.Medium),
    variableFace(R.font.plus_jakarta_sans_wght, FontWeight.SemiBold),
    variableFace(R.font.plus_jakarta_sans_wght, FontWeight.Bold),
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = DomineFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        lineHeight = 64.sp,
        letterSpacing = emTracking(56, -0.02f),
        fontFeatureSettings = TabularFigures,
    ),
    displayMedium = TextStyle(
        fontFamily = DomineFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 52.sp,
        letterSpacing = emTracking(44, -0.01f),
        fontFeatureSettings = TabularFigures,
    ),
    headlineLarge = TextStyle(
        fontFamily = DomineFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = emTracking(32, -0.01f),
        fontFeatureSettings = TabularFigures,
    ),
    headlineMedium = TextStyle(
        fontFamily = DomineFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        fontFeatureSettings = TabularFigures,
    ),
    headlineSmall = TextStyle(
        fontFamily = DomineFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontFeatureSettings = TabularFigures,
    ),
    titleLarge = TextStyle(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontFeatureSettings = TabularFigures,
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.16.sp,
        fontFeatureSettings = TabularFigures,
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.14.sp,
        fontFeatureSettings = TabularFigures,
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.32.sp,
        fontFeatureSettings = TabularFigures,
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.28.sp,
        fontFeatureSettings = TabularFigures,
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.36.sp,
        fontFeatureSettings = TabularFigures,
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.56.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp,
    ),
)
