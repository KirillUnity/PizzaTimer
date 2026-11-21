package com.example.clockplannerproject.kit.core

import androidx.compose.runtime.Immutable
import java.util.UUID

/**
 * One intermediate goal under the single main goal.
 *
 * @param id stable id stored with task links
 * @param title name the user typed
 * @since 1.2.0
 */
@Immutable
data class IntermediateGoal(
    val id: String,
    val title: String,
)

/**
 * One main goal, several intermediate goals, and optional task links.
 * A task with no map entry has no intermediate goal and still exists.
 *
 * @since 1.2.0
 */
@Immutable
data class GoalBook(
    val mainTitle: String = "",
    val intermediate: List<IntermediateGoal> = emptyList(),
    val taskLinks: Map<String, String> = emptyMap(),
) {
    /** Replaces the only main goal. */
    fun withMain(title: String): GoalBook = copy(mainTitle = title.trim())

    /**
     * Adds one intermediate goal. Blank titles are ignored.
     */
    fun addIntermediate(title: String, id: String = UUID.randomUUID().toString()): GoalBook {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return this
        return copy(intermediate = intermediate + IntermediateGoal(id, trimmed))
    }

    /**
     * Links one interval to [intermediateId], or clears it when [intermediateId] is null.
     */
    fun linkInterval(taskId: String, blockId: String, intermediateId: String?): GoalBook =
        link(intervalKey(taskId, blockId), intermediateId)

    /**
     * Links [taskId] to [intermediateId], or clears the link when [intermediateId] is null.
     * An unknown intermediate id clears the link.
     */
    fun link(taskId: String, intermediateId: String?): GoalBook {
        val known = intermediateId?.takeIf { id -> intermediate.any { it.id == id } }
        val next = taskLinks.toMutableMap()
        if (known == null) next.remove(taskId) else next[taskId] = known
        return copy(taskLinks = next)
    }

    /**
     * Copies the intermediate goal of [fromTaskId] onto [nextTaskId] when one exists.
     */
    fun inherit(fromTaskId: String, nextTaskId: String): GoalBook {
        val goalId = taskLinks[fromTaskId] ?: return this
        return link(nextTaskId, goalId)
    }

    companion object {
        /**
         * Key for the intermediate goal of one interval.
         *
         * @param taskId [TaskId.value]
         * @param blockId [TimeBlock.id]
         */
        fun intervalKey(taskId: String, blockId: String): String = "$taskId#$blockId"

        /**
         * The dial does not show the goal list while a session is open.
         *
         * @param sessionOpen true when a work interval is still open
         */
        fun showOnDial(sessionOpen: Boolean): Boolean = sessionOpen && false
    }
}
