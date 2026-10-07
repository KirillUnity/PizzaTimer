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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clockplannerproject.ui.category.CategoriesScreen
import com.example.clockplannerproject.ui.day.DayDestinationContent
import com.example.clockplannerproject.ui.navigation.AppBottomBar
import com.example.clockplannerproject.ui.navigation.AppDestinations
import com.example.clockplannerproject.ui.settings.SettingsScreen
import com.example.clockplannerproject.ui.task.TaskEditorOverlays
import com.example.clockplannerproject.ui.task.TasksDestinationContent
import com.example.clockplannerproject.ui.task.TasksUiEffect
import com.example.clockplannerproject.ui.task.TasksUiIntent
import com.example.clockplannerproject.ui.task.TasksViewModel
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
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
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            AppBottomBar(
                currentDestination = currentDestination,
                onDestinationSelected = { currentDestination = it },
                onQuickCreate = onQuickCreate,
            )
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
                        tasksViewModel?.onIntent(TasksUiIntent.SelectDate(task.date))
                        tasksViewModel?.onIntent(TasksUiIntent.Edit(task))
                    },
                )
                AppDestinations.TASKS -> TasksDestinationContent(
                    modifier = contentModifier,
                    showEditorOverlays = false,
                )
                AppDestinations.CATEGORIES -> CategoriesScreen(modifier = contentModifier)
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

@Preview(showBackground = true)
@Composable
private fun ClockPlannerProjectAppPreview() {
    ClockPlannerProjectTheme {
        ClockPlannerProjectApp()
    }
}
