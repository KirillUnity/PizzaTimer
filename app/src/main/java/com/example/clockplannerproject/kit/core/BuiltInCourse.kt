package com.example.clockplannerproject.kit.core

import androidx.compose.runtime.Immutable

/**
 * One of the three built-in practices from mission §7.2.
 * Not a user-authored lesson.
 *
 * @since 1.2.0
 */
enum class BuiltInPractice {
    HEARTFULNESS,
    YOGA_PAUSE,
    MICRO_QIGONG,
}

/**
 * A fixed lesson: steps, a minute range, and the research named in mission §7.2.
 *
 * Yoga pause has no single author in that section. [citation] therefore names
 * the effects the mission lists (N200, MoCA/MMSE, HRV) and the years 2023–2024.
 *
 * @since 1.2.0
 */
@Immutable
data class CourseLesson(
    val practice: BuiltInPractice,
    val minMinutes: Int,
    val maxMinutes: Int,
    val stepIds: List<String>,
    val citation: String,
)

/**
 * The built-in course. Three practices, no feed, no authoring.
 *
 * @since 1.2.0
 */
object BuiltInCourse {
    /** Tag the user adds when a task needs concentration. Not applied automatically. */
    const val CONCENTRATION_TAG: String = "concentration"

    val lessons: List<CourseLesson> = listOf(
        CourseLesson(
            practice = BuiltInPractice.HEARTFULNESS,
            minMinutes = 3,
            maxMinutes = 5,
            stepIds = listOf("scan", "release", "settle"),
            citation = "Telles et al., 2023, RCT (affect and Digit Letter Substitution). Mission §7.2.",
        ),
        CourseLesson(
            practice = BuiltInPractice.YOGA_PAUSE,
            minMinutes = 2,
            maxMinutes = 3,
            stepIds = listOf("breath", "stretch", "notice"),
            citation = "Mission §7.2 RCTs 2023–2024 (N200 ERP, MoCA/MMSE, HRV). No single author is named there.",
        ),
        CourseLesson(
            practice = BuiltInPractice.MICRO_QIGONG,
            minMinutes = 1,
            maxMinutes = 2,
            stepIds = listOf("stand", "shift", "rest"),
            citation = "Sun et al., 2024; Wang et al., 2014. Mission §7.2.",
        ),
    )

    /**
     * @return true when [task] carries [CONCENTRATION_TAG]
     */
    fun needsConcentration(task: Task?): Boolean =
        task?.tags.orEmpty().any { it.equals(CONCENTRATION_TAG, ignoreCase = true) }

    /**
     * Offer during a break or before a concentration task.
     * An open work interval hides the offer.
     */
    fun offer(workOpen: Boolean, onBreak: Boolean, needsConcentration: Boolean): Boolean {
        if (workOpen) return false
        return onBreak || needsConcentration
    }

    /** @return true when every step checkpoint is checked */
    fun checkpointsMet(lesson: CourseLesson, checked: Set<String>): Boolean =
        lesson.stepIds.all { it in checked }

    fun lesson(practice: BuiltInPractice): CourseLesson =
        lessons.first { it.practice == practice }
}

/**
 * One finished pass. A later pass is another row.
 *
 * @since 1.2.0
 */
@Immutable
data class PracticePass(
    val practice: BuiltInPractice,
    val atEpochMillis: Long,
)

/**
 * Append-only history. A second pass does not replace the first.
 *
 * @since 1.2.0
 */
object CourseHistory {
    fun append(
        history: List<PracticePass>,
        practice: BuiltInPractice,
        atEpochMillis: Long,
    ): List<PracticePass> = history + PracticePass(practice, atEpochMillis)
}
