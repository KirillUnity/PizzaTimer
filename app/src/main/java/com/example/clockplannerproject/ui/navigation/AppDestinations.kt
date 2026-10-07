package com.example.clockplannerproject.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.clockplannerproject.R

/**
 * Top-level destinations. Survives process recreation via [Enum.name]
 * when held in `rememberSaveable`.
 *
 * @since 0.1.0
 */
enum class AppDestinations(
    @StringRes val labelRes: Int,
    @StringRes val contentDescriptionRes: Int,
    val icon: ImageVector,
) {
    DAY(R.string.nav_day, R.string.cd_nav_day, Icons.Filled.DateRange),
    TASKS(R.string.nav_tasks, R.string.cd_nav_tasks, Icons.AutoMirrored.Filled.List),
    CATEGORIES(R.string.nav_categories, R.string.cd_nav_categories, Icons.Filled.Star),
    SETTINGS(R.string.nav_settings, R.string.cd_nav_settings, Icons.Filled.Settings),
}
