package com.example.clockplannerproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
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
import com.example.clockplannerproject.ui.onboarding.OnboardingScreen
import com.example.clockplannerproject.ui.settings.SettingsDestinationContent
import com.example.clockplannerproject.ui.settings.SettingsViewModel
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
        installSplashScreen()
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
        ClockPlannerProjectNav(onQuickCreate = {}, onShowOnboarding = {})
    } else {
        val tasksViewModel: TasksViewModel = koinViewModel()
        val settingsViewModel: SettingsViewModel = koinViewModel()
        val appearance by settingsViewModel.appearance.collectAsStateWithLifecycle()
        val seen by settingsViewModel.onboardingSeen.collectAsStateWithLifecycle()
        var replay by rememberSaveable { mutableStateOf(false) }
        ClockPlannerProjectTheme(darkTheme = appearance.darkTheme) {
        when {
            seen == null -> Box(Modifier.fillMaxSize())
            replay || seen == false -> OnboardingScreen(
                onFinished = {
                    replay = false
                    settingsViewModel.markOnboardingSeen()
                },
            )
            else -> ClockPlannerProjectNav(
                onQuickCreate = { tasksViewModel.onIntent(TasksUiIntent.Create) },
                tasksViewModel = tasksViewModel,
                onShowOnboarding = { replay = true },
            )
        }
        }
    }
}

@Composable
private fun ClockPlannerProjectNav(
    onQuickCreate: () -> Unit,
    onShowOnboarding: () -> Unit,
    tasksViewModel: TasksViewModel? = null,
) {
    var currentDestination by rememberSaveable {
        mutableStateOf(AppDestinations.DAY)
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    if (tasksViewModel != null) {
        LaunchedEffect(tasksViewModel) {
            tasksViewModel.effect.collect { effect ->
                when (effect) {
                    is TasksUiEffect.ShowMessageRes ->
                        snackbarHostState.showSnackbar(context.getString(effect.resId))
                }
            }
        }
    }
    val showFab = currentDestination == AppDestinations.TASKS
    val suiteType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
        currentWindowAdaptiveInfo(),
    )
    // The suite bar already sits above the system navigation bar. Padding that
    // inset again pushes the dial and lists up. Rail and drawer leave the
    // gesture bar under the content, so that case keeps the bottom inset.
    val contentWindowInsets = if (suiteType == NavigationSuiteType.NavigationBar) {
        WindowInsets.safeDrawing.exclude(WindowInsets.navigationBars).union(WindowInsets.ime)
    } else {
        WindowInsets.safeDrawing.union(WindowInsets.ime)
    }
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
            contentWindowInsets = contentWindowInsets,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                if (showFab) {
                    val onFabClick = {
                        if (currentDestination == AppDestinations.DAY && tasksViewModel != null) {
                            tasksViewModel.onIntent(TasksUiIntent.CreateForSelectedDay)
                        } else {
                            onQuickCreate()
                        }
                    }
                    if (currentDestination == AppDestinations.TASKS) {
                        ExtendedFloatingActionButton(
                            onClick = onFabClick,
                            containerColor = TerracottaNow,
                            contentColor = OnTerracotta,
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                )
                            },
                            text = { Text(stringResource(R.string.fab_add_task)) },
                        )
                    } else {
                        ExtendedFloatingActionButton(
                            onClick = onFabClick,
                            containerColor = TerracottaNow,
                            contentColor = OnTerracotta,
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                )
                            },
                            text = { Text(stringResource(R.string.fab_add_task)) },
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
                        onOpenReports = { currentDestination = AppDestinations.REPORTS },
                    )
                    AppDestinations.TASKS -> TasksDestinationContent(
                        modifier = contentModifier,
                        showEditorOverlays = false,
                    )
                    AppDestinations.REPORTS -> ReportsDestinationContent(modifier = contentModifier)
                    AppDestinations.SETTINGS -> SettingsDestinationContent(
                        onShowOnboarding = onShowOnboarding,
                        onOpenDay = { currentDestination = AppDestinations.DAY },
                        modifier = contentModifier,
                    )
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
