package com.example.clockplannerproject.kit.core

import androidx.compose.runtime.Immutable

/**
 * One way to make the next similar task a little harder.
 * Agreeing records the kind. It does not award points.
 *
 * @since 1.2.0
 */
enum class HardenKind {
    ALREADY_A_STEP,
    EXPLICIT_REQUIREMENT,
    NARROWER_RESULT,
}

/**
 * One offer to harden the next task that shares a title.
 *
 * @param key identity of the three completions, so a skip can hide them
 * @since 1.2.0
 */
@Immutable
data class DifficultyOffer(
    val title: String,
    val key: String,
)

/**
 * Decides when a habitual task should be a little harder.
 * Skill and engagement are the 1–10 self-report proxies, not a flow score.
 *
 * @since 1.2.0
 */
object DifficultyNudge {
    const val SKILL_AT_LEAST: Int = 8
    const val ENGAGEMENT_AT_MOST: Int = 4

    /**
     * One finished task in title order. [at] is the completion timestamp.
     */
    data class Row(
        val taskId: String,
        val title: String,
        val completion: TaskCompletion,
        val at: Long,
    )

    /**
     * @param rows completions, any titles
     * @param skipped keys already dismissed
     * @return an offer for the newest qualifying trio of one title, or null
     */
    fun offer(rows: List<Row>, skipped: Set<String>): DifficultyOffer? {
        return rows
            .groupBy { it.title.trim() }
            .mapNotNull { (title, group) ->
                if (title.isEmpty()) return@mapNotNull null
                val last = group.sortedBy { it.at }.takeLast(3)
                if (last.size < 3) return@mapNotNull null
                val qualifies = last.all { row ->
                    row.completion.skill >= SKILL_AT_LEAST &&
                        row.completion.engagement <= ENGAGEMENT_AT_MOST
                }
                if (!qualifies) return@mapNotNull null
                val key = last.joinToString("|") { it.taskId }
                if (key in skipped) return@mapNotNull null
                DifficultyOffer(title = title, key = key)
            }
            .firstOrNull()
    }

    /**
     * Records [kind] on [task] without touching [Task.blocks].
     */
    fun apply(task: Task, kind: HardenKind): Task {
        val line = "nudge:${kind.name}"
        val description = if (task.description.isBlank()) line else task.description + "\n" + line
        return task.copy(description = description)
    }
}
