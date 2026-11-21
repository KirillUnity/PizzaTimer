package com.example.clockplannerproject.kit.core

import androidx.compose.runtime.Immutable

/**
 * One demand mark on a completion. A completion stores exactly one.
 *
 * @since 1.2.0
 */
enum class DemandMark {
    CHALLENGE,
    HINDRANCE,
}

/**
 * Self-report proxies saved on a task.
 *
 * [stateAfter], [engagement], and [skill] are integers from 1 to 10. They are
 * a self-report, not a clinical scale, and they are not an FSS-2 score.
 * Nothing in this type is stored inside a [TimeBlock].
 *
 * @param easier short note of what got easier
 * @param thoughts thoughts that got in the way; may be empty
 * @param reframe optional note on how a hindrance could become a challenge
 * @param progress line kept when the task is not marked done
 * @since 1.2.0
 */
@Immutable
data class TaskCompletion(
    val stateAfter: Int,
    val engagement: Int,
    val skill: Int,
    val easier: String,
    val mark: DemandMark,
    val thoughts: String,
    val reframe: String,
    val done: Boolean,
    val progress: String,
    /**
     * Nine short-form answers, or null when the form was skipped.
     * Not mixed into [stateAfter], [engagement], or [skill].
     */
    val shortFlow: List<Int>? = null,
) {
    init {
        require(stateAfter in SCORE_RANGE)
        require(engagement in SCORE_RANGE)
        require(skill in SCORE_RANGE)
        require(shortFlow == null || ShortFlowScale.profileOrNull(shortFlow) != null)
    }

    companion object {
        /** Inclusive proxy range. Not a clinical instrument. */
        val SCORE_RANGE: IntRange = 1..10

        /** Filled in when Save is tapped with a proxy still unset. */
        const val DEFAULT_PROXY: Int = 5

        /**
         * @return a completion, or null when a proxy is missing or out of range
         * or [mark] is null
         */
        fun orNull(
            stateAfter: Int?,
            engagement: Int?,
            skill: Int?,
            easier: String,
            mark: DemandMark?,
            thoughts: String,
            reframe: String,
            done: Boolean,
            progress: String,
            shortFlow: List<Int>? = null,
        ): TaskCompletion? {
            if (stateAfter == null || engagement == null || skill == null || mark == null) {
                return null
            }
            if (stateAfter !in SCORE_RANGE || engagement !in SCORE_RANGE || skill !in SCORE_RANGE) {
                return null
            }
            return TaskCompletion(
                stateAfter = stateAfter,
                engagement = engagement,
                skill = skill,
                easier = singleLine(easier),
                mark = mark,
                thoughts = singleLine(thoughts),
                reframe = singleLine(reframe),
                done = done,
                progress = singleLine(progress),
                shortFlow = ShortFlowScale.profileOrNull(shortFlow),
            )
        }

        private fun singleLine(value: String): String =
            value.trim().replace('\n', ' ')
    }
}

/**
 * Encodes [TaskCompletion] into a report body so scores stay on the task
 * without a new Room column.
 *
 * @since 1.2.0
 */
object TaskCompletionCodec {
    const val PREFIX: String = "timedial-completion/v1"

    /** Report id for the single completion of [taskId]. Kept for older rows. */
    fun reportId(taskId: TaskId): ReportId = ReportId("completion:${taskId.value}")

    /**
     * Report id for one interval. Scores for two intervals of the same task do not share a row.
     *
     * @param blockId [TimeBlock.id] of the interval being closed
     */
    fun reportId(taskId: TaskId, blockId: String): ReportId =
        ReportId("completion:${taskId.value}:$blockId")

    /** True when [text] is a completion record rather than a user note. */
    fun isRecord(text: String): Boolean = text.startsWith(PREFIX)

    /**
     * @return the stored completion, or null when [text] is not a record
     * or a required proxy is missing
     */
    fun decode(text: String): TaskCompletion? {
        if (!isRecord(text)) return null
        val fields = text.lineSequence().drop(1).mapNotNull { line ->
            val split = line.indexOf('=')
            if (split <= 0) null else line.substring(0, split) to line.substring(split + 1)
        }.toMap()
        return TaskCompletion.orNull(
            stateAfter = fields["state"]?.toIntOrNull(),
            engagement = fields["engagement"]?.toIntOrNull(),
            skill = fields["skill"]?.toIntOrNull(),
            easier = fields["easier"].orEmpty(),
            mark = fields["mark"]?.let { raw ->
                DemandMark.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
            },
            thoughts = fields["thoughts"].orEmpty(),
            reframe = fields["reframe"].orEmpty(),
            done = fields["done"] == "true",
            progress = fields["progress"].orEmpty(),
            shortFlow = fields["fss"]?.split(',')?.mapNotNull { it.toIntOrNull() },
        )
    }

    /** @return a report body that [decode] can read back */
    fun encode(completion: TaskCompletion): String = buildString {
        append(PREFIX)
        append('\n')
        append("state=${completion.stateAfter}\n")
        append("engagement=${completion.engagement}\n")
        append("skill=${completion.skill}\n")
        append("easier=${completion.easier}\n")
        append("mark=${completion.mark.name}\n")
        append("thoughts=${completion.thoughts}\n")
        append("reframe=${completion.reframe}\n")
        append("done=${completion.done}\n")
        append("progress=${completion.progress}")
        val flow = completion.shortFlow
        if (flow != null) {
            append("\n")
            append("fss=${flow.joinToString(",")}")
        }
    }
}
