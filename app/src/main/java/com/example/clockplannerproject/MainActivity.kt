package com.example.clockplannerproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clockplannerproject.ui.day.DayDestinationContent
import com.example.clockplannerproject.ui.navigation.AppDestinations
import com.example.clockplannerproject.ui.settings.SettingsScreen
import com.example.clockplannerproject.ui.stats.ReportsDestinationContent
import com.example.clockplannerproject.ui.task.TaskEditorOverlays
import com.example.clockplannerproject.ui.task.TasksDestinationContent
import com.example.clockplannerproject.ui.task.TasksUiEffect
import com.example.clockplannerproject.ui.task.TasksUiIntent
import com.example.clockplannerproject.ui.task.TasksViewModel
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import com.example.clockplannerproject.ui.theme.OnTerracotta
import com.example.clockplannerproject.ui.theme.TerracottaNow
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClockPlannerProjectTheme {
                ClockPlannerProjectApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun ClockPlannerProjectApp() {
    if (LocalInspectionMode.current) {
        ClockPlannerProjectNav(onQuickCreate = {})
    } else {
        val tasksViewModel: TasksViewModel = koinViewModel()
        ClockPlannerProjectNav(
            onQuickCreate = { tasksViewModel.onIntent(TasksUiIntent.Create) },
            tasksViewModel = tasksViewModel,
        )
    }
}

@Composable
private fun ClockPlannerProjectNav(
    onQuickCreate: () -> Unit,
    tasksViewModel: TasksViewModel? = null,
) {
    var currentDestination by rememberSaveable {
        mutableStateOf(AppDestinations.DAY)
    }
    val snackbarHostState = remember { SnackbarHostState() }
    if (tasksViewModel != null) {
        LaunchedEffect(tasksViewModel) {
            tasksViewModel.effect.collect { effect ->
                when (effect) {
                    is TasksUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }
    val showFab = currentDestination == AppDestinations.DAY ||
        currentDestination == AppDestinations.TASKS
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    selected = currentDestination == destination,
                    onClick = { currentDestination = destination },
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = stringResource(destination.contentDescriptionRes),
                        )
                    },
                    label = { Text(stringResource(destination.labelRes)) },
                )
            }
        },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                if (showFab) {
                    FloatingActionButton(
                        onClick = {
                            if (currentDestination == AppDestinations.DAY && tasksViewModel != null) {
                                tasksViewModel.onIntent(TasksUiIntent.CreateForSelectedDay)
                            } else {
                                onQuickCreate()
                            }
                        },
                        containerColor = TerracottaNow,
                        contentColor = OnTerracotta,
                        shape = CircleShape,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.fab_log_sector),
                        )
                    }
                }
            },
        ) { innerPadding ->
            val contentModifier = Modifier.padding(innerPadding)
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "destination",
            ) { destination ->
                when (destination) {
                    AppDestinations.DAY -> DayDestinationContent(
                        modifier = contentModifier,
                        onAddTask = onQuickCreate,
                        onEditTask = { task ->
                            task.date?.let { date ->
                                tasksViewModel?.onIntent(TasksUiIntent.SelectDate(date))
                            }
                            tasksViewModel?.onIntent(TasksUiIntent.Edit(task))
                        },
                    )
                    AppDestinations.TASKS -> TasksDestinationContent(
                        modifier = contentModifier,
                        showEditorOverlays = false,
                    )
                    AppDestinations.REPORTS -> ReportsDestinationContent(modifier = contentModifier)
                    AppDestinations.SETTINGS -> SettingsScreen(modifier = contentModifier)
                }
            }
            if (tasksViewModel != null) {
                val tasksState by tasksViewModel.state.collectAsStateWithLifecycle()
                TaskEditorOverlays(
                    state = tasksState,
                    onIntent = tasksViewModel::onIntent,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClockPlannerProjectAppPreview() {
    ClockPlannerProjectTheme {
        ClockPlannerProjectApp()
    }
}
