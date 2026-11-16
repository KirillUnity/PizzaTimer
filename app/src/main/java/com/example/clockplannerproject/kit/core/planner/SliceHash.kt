package com.example.clockplannerproject.kit.core.planner

import com.example.clockplannerproject.kit.core.RecurrenceRule
import com.example.clockplannerproject.kit.core.Task
import java.security.MessageDigest

/**
 * Stable hash of the task slice sent with a plan request.
 *
 * Color is omitted. Descriptions are cut to [SLICE_DESCRIPTION_LIMIT].
 * Task order does not change the hash.
 *
 * @since 1.1.0
 */
object SliceHash {
    /**
     * @param tasks instances and templates included in the request
     * @return lowercase hex SHA-256 of the canonical slice
     * @since 1.1.0
     */
    fun of(tasks: List<Task>): String {
        val canonical = tasks
            .map(::canonicalTask)
            .sorted()
            .joinToString(separator = "\u001e")
        val digest = MessageDigest.getInstance("SHA-256").digest(
            canonical.toByteArray(Charsets.UTF_8),
        )
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    private fun canonicalTask(task: Task): String = buildString {
        append(task.id.value)
        append('\u001f')
        append(task.title)
        append('\u001f')
        append(task.description.take(SLICE_DESCRIPTION_LIMIT))
        append('\u001f')
        task.blocks.forEach { block ->
            append(block.startMinute)
            append('-')
            append(block.endMinute?.toString() ?: "open")
            append(',')
        }
        append('\u001f')
        append(task.status.name)
        append('\u001f')
        append(task.date?.toString().orEmpty())
        append('\u001f')
        append(task.importance.name)
        append('\u001f')
        append(task.normalizedProject.orEmpty())
        append('\u001f')
        append(task.normalizedTags.joinToString(separator = ","))
        append('\u001f')
        append(recurrenceToken(task.recurrence))
        append('\u001f')
        append(task.seriesId.orEmpty())
    }

    private fun recurrenceToken(rule: RecurrenceRule): String = when (rule) {
        RecurrenceRule.None -> "none"
        RecurrenceRule.Daily -> "daily"
        is RecurrenceRule.Weekdays ->
            "weekdays:" + rule.days.map { day -> day.value }.sorted().joinToString(",")
    }
}
