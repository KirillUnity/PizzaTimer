package com.example.clockplannerproject.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R

/**
 * Day, Tasks, Create (center, not a destination), Categories, Settings.
 *
 * @since 0.2.0
 */
@Composable
fun AppBottomBar(
    currentDestination: AppDestinations,
    onDestinationSelected: (AppDestinations) -> Unit,
    onQuickCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        barItem(AppDestinations.DAY, currentDestination, onDestinationSelected)
        barItem(AppDestinations.TASKS, currentDestination, onDestinationSelected)
        NavigationBarItem(
            selected = false,
            onClick = onQuickCreate,
            icon = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.cd_create_task),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            },
            label = { Text(stringResource(R.string.nav_create)) },
        )
        barItem(AppDestinations.CATEGORIES, currentDestination, onDestinationSelected)
        barItem(AppDestinations.SETTINGS, currentDestination, onDestinationSelected)
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
