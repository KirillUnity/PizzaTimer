package com.example.clockplannerproject.ui.settings.practice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clockplannerproject.kit.core.practice.PracticeSkill
import org.koin.androidx.compose.koinViewModel

/**
 * Practice hosted inside Settings. The suite destination stays Settings
 * until [onOpenDay] switches it to the day.
 *
 * @param onBack returns to the settings list
 * @param onOpenDay selects the Day destination
 * @param onShowIntroduction opens the existing four-page intro
 * @since 1.3.0
 */
@Composable
fun PracticeDestination(
    onBack: () -> Unit,
    onOpenDay: () -> Unit,
    onShowIntroduction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var lessonName by rememberSaveable { mutableStateOf<String?>(null) }
    val lesson = lessonName?.let { runCatching { PracticeSkill.valueOf(it) }.getOrNull() }
    if (lesson != null) {
        PracticeLesson(
            skill = lesson,
            onOpenDay = onOpenDay,
            onBack = { lessonName = null },
            modifier = modifier,
        )
        return
    }
    if (LocalInspectionMode.current) {
        PracticeHub(
            tried = emptySet(),
            week = emptyList(),
            onOpenLesson = { lessonName = it.name },
            onOpenDay = onOpenDay,
            onShowIntroduction = onShowIntroduction,
            onBack = onBack,
            modifier = modifier,
        )
    } else {
        val viewModel: PracticeViewModel = koinViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        PracticeHub(
            tried = state.tried,
            week = state.week,
            onOpenLesson = { lessonName = it.name },
            onOpenDay = onOpenDay,
            onShowIntroduction = onShowIntroduction,
            onBack = onBack,
            modifier = modifier,
        )
    }
}
