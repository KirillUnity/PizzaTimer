package com.example.clockplannerproject.kit.core

import androidx.compose.runtime.Immutable

/**
 * Self-report totals for one day. Each field is its own number or list.
 * The 1–10 values are proxies, not a flow score and not a clinical scale.
 *
 * @param states state-after proxies of completed tasks, in task order
 * @param thoughts non-empty thought lines from those completions
 * @param meanSkill average skill proxy, or 0 when none are completed
 * @param easier non-empty “what got easier” lines
 * @param startedCount tasks on the day that have at least one interval
 * @since 1.2.0
 */
@Immutable
data class DayStateStats(
    val states: List<Int>,
    val hindranceCount: Int,
    val challengeCount: Int,
    val thoughts: List<String>,
    val meanSkill: Double,
    val easier: List<String>,
    val startedCount: Int,
    /** Nine short-form means, empty when the form was skipped. Not part of [meanSkill]. */
    val formProfile: List<Double> = emptyList(),
) {
    companion object {
        /** Zeros and empty lines. Not a warning. */
        val Empty: DayStateStats = DayStateStats(
            states = emptyList(),
            hindranceCount = 0,
            challengeCount = 0,
            thoughts = emptyList(),
            meanSkill = 0.0,
            easier = emptyList(),
            startedCount = 0,
        )

        /**
         * @param tasks tasks that belong to the day, including breaks
         * @param reports every stored report; only completion records for [tasks] count
         */
        fun of(tasks: List<Task>, reports: List<TaskReport>): DayStateStats {
            val work = tasks.filterNot { BreakSession.isBreak(it) }
            val byId = reports.mapNotNull { report ->
                val completion = TaskCompletionCodec.decode(report.text) ?: return@mapNotNull null
                report.taskId to completion
            }.toMap()
            val completed = work.mapNotNull { task ->
                byId[task.id]?.takeIf { it.done }
            }
            if (work.isEmpty() && completed.isEmpty()) return Empty
            val skills = completed.map { it.skill }
            return DayStateStats(
                states = completed.map { it.stateAfter },
                hindranceCount = completed.count { it.mark == DemandMark.HINDRANCE },
                challengeCount = completed.count { it.mark == DemandMark.CHALLENGE },
                thoughts = completed.map { it.thoughts }.filter { it.isNotEmpty() },
                meanSkill = if (skills.isEmpty()) 0.0 else skills.average(),
                easier = completed.map { it.easier }.filter { it.isNotEmpty() },
                startedCount = work.count { it.blocks.isNotEmpty() },
                formProfile = ShortFlowScale.means(completed.mapNotNull { it.shortFlow }),
            )
        }

        /**
         * The day dial does not show this summary while a session is open.
         *
         * @param sessionOpen true when any interval is still open
         */
        fun showOnDial(sessionOpen: Boolean): Boolean = !sessionOpen && false
    }
}
