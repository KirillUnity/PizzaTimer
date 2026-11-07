package com.example.clockplannerproject.kit.core.planner

import com.example.clockplannerproject.kit.core.Importance
import kotlinx.datetime.LocalDate

/**
 * Line encoding for accepted plans inside DataStore preferences.
 *
 * One plan per block, separated by a blank line. Field text is escaped so
 * titles can contain separators.
 *
 * @since 1.1.0
 */
object AcceptedPlanCodec {
    /**
     * @param plans plans to persist; order is kept
     * @return text stored under one preference key, or an empty string
     * @since 1.1.0
     */
    fun encode(plans: List<AcceptedPlan>): String =
        plans.joinToString(separator = BLOCK_SEPARATOR) { plan -> encodePlan(plan) }

    /**
     * @param raw value previously produced by [encode], or blank
     * @return decoded plans; a damaged block is skipped
     * @since 1.1.0
     */
    fun decode(raw: String): List<AcceptedPlan> {
        if (raw.isBlank()) return emptyList()
        return raw.split(BLOCK_SEPARATOR).mapNotNull { block -> decodePlan(block) }
    }

    private fun encodePlan(plan: AcceptedPlan): String = buildString {
        append(PLAN)
        append(FIELD)
        append(escape(plan.id))
        append(FIELD)
        append(escape(plan.goal))
        append(FIELD)
        append(plan.deadline)
        append('\n')
        plan.steps.forEach { step ->
            append(STEP)
            append(FIELD)
            append(escape(step.title))
            append(FIELD)
            append(step.date?.toString().orEmpty())
            append(FIELD)
            append(step.startMinute?.toString().orEmpty())
            append(FIELD)
            append(step.endMinute?.toString().orEmpty())
            append(FIELD)
            append(if (step.unplaced) "1" else "0")
            append(FIELD)
            append(step.estimateMinutes)
            append(FIELD)
            append(step.bufferedMinutes)
            append(FIELD)
            append(step.importance.name)
            append(FIELD)
            append(escape(step.project.orEmpty()))
            append('\n')
        }
    }

    private fun decodePlan(block: String): AcceptedPlan? {
        val lines = block.lineSequence().filter { it.isNotBlank() }.toList()
        val header = lines.firstOrNull { it.startsWith(PLAN_PREFIX) } ?: return null
        val headerParts = header.split(FIELD)
        if (headerParts.size < PLAN_COLUMNS) return null
        val deadline = runCatching { LocalDate.parse(headerParts[3]) }.getOrNull() ?: return null
        val steps = lines.filter { it.startsWith(STEP_PREFIX) }.mapNotNull { line ->
            decodeStep(line)
        }
        return AcceptedPlan(
            id = unescape(headerParts[1]),
            goal = unescape(headerParts[2]),
            deadline = deadline,
            steps = steps,
        )
    }

    private fun decodeStep(line: String): PlacedStep? {
        val parts = line.split(FIELD)
        if (parts.size < STEP_COLUMNS) return null
        val importance = runCatching { Importance.valueOf(parts[8]) }.getOrDefault(Importance.MEDIUM)
        val date = parts[2].takeIf { it.isNotEmpty() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        return PlacedStep(
            title = unescape(parts[1]),
            date = date,
            startMinute = parts[3].toIntOrNull(),
            endMinute = parts[4].toIntOrNull(),
            unplaced = parts[5] == "1",
            estimateMinutes = parts[6].toIntOrNull() ?: return null,
            bufferedMinutes = parts[7].toIntOrNull() ?: return null,
            importance = importance,
            project = unescape(parts[9]).takeIf { it.isNotEmpty() },
        )
    }

    private fun escape(value: String): String = buildString(value.length) {
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '|' -> append("\\p")
                else -> append(char)
            }
        }
    }

    private fun unescape(value: String): String = buildString(value.length) {
        var index = 0
        while (index < value.length) {
            val char = value[index]
            if (char == '\\' && index + 1 < value.length) {
                when (value[index + 1]) {
                    '\\' -> append('\\')
                    'n' -> append('\n')
                    'p' -> append('|')
                    else -> append(value[index + 1])
                }
                index += 2
            } else {
                append(char)
                index += 1
            }
        }
    }

    private const val PLAN: String = "plan"
    private const val STEP: String = "step"
    private const val PLAN_PREFIX: String = "plan|"
    private const val STEP_PREFIX: String = "step|"
    private const val FIELD: String = "|"
    private const val BLOCK_SEPARATOR: String = "\n\n"
    private const val PLAN_COLUMNS: Int = 4
    private const val STEP_COLUMNS: Int = 10
}
