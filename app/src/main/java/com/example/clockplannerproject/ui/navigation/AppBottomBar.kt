package com.example.clockplannerproject.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * Day, Tasks, Reports, Settings. Create uses the terracotta FAB, not a nav slot.
 *
 * @since 0.2.0
 */
@Composable
fun AppBottomBar(
    currentDestination: AppDestinations,
    onDestinationSelected: (AppDestinations) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        AppDestinations.entries.forEach { destination ->
            barItem(destination, currentDestination, onDestinationSelected)
        }
    }
}

@Composable
private fun RowScope.barItem(
    destination: AppDestinations,
    current: AppDestinations,
    onSelected: (AppDestinations) -> Unit,
) {
    NavigationBarItem(
        selected = current == destination,
        onClick = { onSelected(destination) },
        icon = {
            Icon(
                imageVector = destination.icon,
                contentDescription = stringResource(destination.contentDescriptionRes),
            )
        },
        label = { Text(stringResource(destination.labelRes)) },
    )
}
