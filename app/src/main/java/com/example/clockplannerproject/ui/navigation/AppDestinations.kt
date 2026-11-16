package com.example.clockplannerproject.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
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
    DAY(R.string.nav_day, R.string.cd_nav_day, Icons.Filled.Schedule),
    TASKS(R.string.nav_tasks, R.string.cd_nav_tasks, Icons.Filled.Checklist),
    REPORTS(R.string.nav_reports, R.string.cd_nav_reports, Icons.Filled.BarChart),
    SETTINGS(R.string.nav_settings, R.string.cd_nav_settings, Icons.Filled.Settings),
}
