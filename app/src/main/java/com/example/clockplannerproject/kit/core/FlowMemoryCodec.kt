package com.example.clockplannerproject.kit.core

import kotlinx.datetime.LocalDate

/**
 * Encodes pause notes and local dates for a preferences string set.
 *
 * A corrupt entry decodes to null so one bad value does not drop the rest.
 *
 * @since 1.2.0
 */
object FlowMemoryCodec {
    private const val SEPARATOR = "\u001f"

    /**
     * @return one preferences entry for [taskId]
     */
    fun encodeCheckpoint(taskId: TaskId, checkpoint: PauseCheckpoint): String =
        listOf(
            escape(taskId.value),
            checkpoint.mood.name,
            escape(checkpoint.text),
        ).joinToString(SEPARATOR)

    /**
     * @return the task and note, or null when [raw] is not a known entry
     */
    fun decodeCheckpoint(raw: String): Pair<TaskId, PauseCheckpoint>? {
        val parts = raw.split(SEPARATOR, limit = 3)
        if (parts.size != 3) return null
        val mood = pauseMoodOrNull(parts[1]) ?: return null
        val text = unescape(parts[2]).trim()
        if (text.isEmpty()) return null
        return TaskId(unescape(parts[0])) to PauseCheckpoint(text, mood)
    }

    /** @return true when [raw] belongs to [taskId] */
    fun sameTask(raw: String, taskId: TaskId): Boolean =
        decodeCheckpoint(raw)?.first == taskId

    /** @return ISO-8601 local date */
    fun encodeDate(date: LocalDate): String = date.toString()

    /**
     * @return the date, or null when [raw] is not an ISO local date
     */
    fun decodeDate(raw: String): LocalDate? =
        try {
            LocalDate.parse(raw)
        } catch (_: IllegalArgumentException) {
            null
        }

    private fun pauseMoodOrNull(name: String): PauseMood? =
        try {
            PauseMood.valueOf(name)
        } catch (_: IllegalArgumentException) {
            null
        }

    private fun escape(value: String): String = buildString {
        for (ch in value) {
            when (ch) {
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                SEPARATOR.single() -> append("\\s")
                else -> append(ch)
            }
        }
    }

    private fun unescape(value: String): String = buildString {
        var index = 0
        while (index < value.length) {
            val ch = value[index]
            if (ch == '\\' && index + 1 < value.length) {
                when (value[index + 1]) {
                    '\\' -> append('\\')
                    'n' -> append('\n')
                    'r' -> append('\r')
                    's' -> append(SEPARATOR)
                    else -> append(value[index + 1])
                }
                index += 2
            } else {
                append(ch)
                index += 1
            }
        }
    }
}
