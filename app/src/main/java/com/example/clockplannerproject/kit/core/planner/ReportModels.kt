package com.example.clockplannerproject.kit.core.planner

import androidx.compose.runtime.Immutable
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.time.TimeMath
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

/**
 * Accepted plan stored on the device after the user places a draft on the dial.
 *
 * @param id stable id chosen by the caller when the user accepts a draft
 * @param goal goal title that was accepted
 * @param deadline deadline of that draft
 * @param steps steps that were actually written; unplaced rows may be omitted
 * @since 1.1.0
 */
@Immutable
data class AcceptedPlan(
    val id: String,
    val goal: String,
    val deadline: LocalDate,
    val steps: List<PlacedStep>,
)

/**
 * Local report request. Replaces `POST /v1/reports`.
 *
 * @param from first local day of the period, inclusive
 * @param to last local day of the period, inclusive
 * @param zone IANA zone reported by the client; the mock does not send it
 * @param tasks tasks the phone already stores for the period
 * @param acceptedPlans plans read from DataStore; empty means nothing to compare
 * @since 1.1.0
 */
@Immutable
data class ReportRequest(
    val from: LocalDate,
    val to: LocalDate,
    val zone: TimeZone,
    val tasks: List<Task> = emptyList(),
    val acceptedPlans: List<AcceptedPlan> = emptyList(),
)

/**
 * Report paragraph and the minute totals that were substituted into it.
 *
 * @param text template paragraph; an empty period is still a success
 * @param doneMinutes sum of closed-block lengths inside the period
 * @param plannedMinutes sum of placed accepted-step lengths whose day is in the period
 * @since 1.1.0
 */
@Immutable
data class ReportResult(
    val text: String,
    val doneMinutes: Int,
    val plannedMinutes: Int,
)

/**
 * Builds a report paragraph from closed blocks and accepted plans.
 *
 * Open timers add nothing, including on a future day. A done task with no
 * blocks adds 0 minutes and contributes its title only. Comparison text is
 * included only when [ReportRequest.acceptedPlans] has a step dated inside
 * the period.
 *
 * @since 1.1.0
 */
object ReportSummary {
    /**
     * @param request period, tasks, and accepted plans already loaded on the device
     * @param phrases localized templates; numbers are inserted from this summary
     * @return paragraph plus the minute totals used in that paragraph
     * @since 1.1.0
     */
    fun build(request: ReportRequest, phrases: PlannerPhrases): ReportResult {
        var doneMinutes = 0
        val untimedDone = mutableListOf<String>()
        for (task in request.tasks) {
            val date = task.date ?: continue
            if (date < request.from || date > request.to) continue
            val closed = task.blocks.filter { block -> !block.isOpen }
            if (closed.isEmpty()) {
                if (task.status == TaskStatus.DONE && task.blocks.isEmpty()) {
                    untimedDone += task.title
                }
                continue
            }
            for (block in closed) {
                val end = block.endMinute ?: continue
                doneMinutes += TimeMath.durationMinutes(block.startMinute, end)
            }
        }
        var plannedMinutes = 0
        var hasPlanInPeriod = false
        for (plan in request.acceptedPlans) {
            for (step in plan.steps) {
                val date = step.date ?: continue
                if (date < request.from || date > request.to) continue
                hasPlanInPeriod = true
                if (!step.unplaced) plannedMinutes += step.placedMinutes
            }
        }
        val parts = mutableListOf<String>()
        parts += if (doneMinutes == 0) {
            phrases.reportNoClosedIntervals()
        } else {
            phrases.reportDone(doneMinutes)
        }
        if (untimedDone.isNotEmpty()) {
            parts += phrases.reportUntimedDone(untimedDone.joinToString(", "))
        }
        parts += if (hasPlanInPeriod) {
            phrases.reportComparison(plannedMinutes, doneMinutes)
        } else {
            phrases.reportNothingToCompare()
        }
        return ReportResult(
            text = parts.joinToString(" "),
            doneMinutes = doneMinutes,
            plannedMinutes = plannedMinutes,
        )
    }
}

/**
 * Why [PlannerSource.report] returned no paragraph.
 *
 * This is the same kind of refusal as an unreachable planner: the caller
 * shows [message] and does not write a note or delete tasks.
 *
 * @since 1.1.0
 */
sealed interface ReportRejection {
    /** User-facing sentence loaded from resources (or the JVM mirror). */
    val message: String

    /**
     * Mock stand-in for an unreachable service. No paragraph is built.
     *
     * @param message localized explanation, the same sentence as plan refusal
     */
    data class Unavailable(override val message: String) : ReportRejection
}

/**
 * Result of a report request. [Rejected] writes nothing.
 *
 * @since 1.1.0
 */
sealed interface ReportOutcome {
    /**
     * @param result paragraph and the minute totals substituted into it
     */
    data class Ready(val result: ReportResult) : ReportOutcome

    /**
     * @param reason availability failure; the note list stays as it was
     */
    data class Rejected(val reason: ReportRejection) : ReportOutcome
}

/**
 * Inclusive local days for a manual report.
 *
 * A week is seven local days ending today, including today.
 * Nothing here runs on a schedule.
 *
 * @since 1.1.0
 */
object ReportPeriod {
    /** Seven local days, including today. */
    const val WEEK_LENGTH_DAYS: Int = 7

    /**
     * Inclusive range passed to [ReportRequest].
     *
     * @param from first local day, inclusive
     * @param to last local day, inclusive
     */
    @Immutable
    data class Inclusive(
        val from: LocalDate,
        val to: LocalDate,
    )

    /**
     * One local day.
     *
     * @param day the day the user asked to collect
     */
    fun day(day: LocalDate): Inclusive = Inclusive(day, day)

    /**
     * Seven local days ending on [today], including [today].
     *
     * @param today device-local today
     */
    fun weekEnding(today: LocalDate): Inclusive {
        val from = today.minus(WEEK_LENGTH_DAYS - 1, DateTimeUnit.DAY)
        return Inclusive(from, today)
    }
}

/**
 * Task a collected report note hangs on.
 *
 * Prefers an untimed task whose title equals an accepted plan goal that has
 * a step inside the period. Otherwise one untimed task titled with the
 * fallback (the caller creates it once and later presses reuse it).
 *
 * @since 1.1.0
 */
object ReportAnchor {
    /**
     * @param tasks every task already stored on the device
     * @param plans accepted plans from DataStore
     * @param from first local day of the report, inclusive
     * @param to last local day of the report, inclusive
     * @param fallbackTitle title used when no goal anchor exists, `Отчёт yyyy-mm-dd`
     * @return an existing task id, or a title the caller must create
     */
    fun choose(
        tasks: List<Task>,
        plans: List<AcceptedPlan>,
        from: LocalDate,
        to: LocalDate,
        fallbackTitle: String,
    ): Target {
        for (plan in plans) {
            if (!planHasStepInPeriod(plan, from, to)) continue
            val goal = plan.goal.trim()
            val match = tasks.firstOrNull { task ->
                task.blocks.isEmpty() && task.title.trim() == goal
            }
            if (match != null) return Target.Existing(match.id)
        }
        val titled = tasks.firstOrNull { task ->
            task.blocks.isEmpty() && task.title.trim() == fallbackTitle.trim()
        }
        if (titled != null) return Target.Existing(titled.id)
        return Target.Create(fallbackTitle)
    }

    private fun planHasStepInPeriod(plan: AcceptedPlan, from: LocalDate, to: LocalDate): Boolean =
        plan.steps.any { step ->
            val date = step.date ?: return@any false
            date >= from && date <= to
        }

    /**
     * Where [com.example.clockplannerproject.kit.core.ReportRepository.upsert] should attach the note.
     *
     * @since 1.1.0
     */
    sealed interface Target {
        /**
         * @param taskId untimed task that already exists
         */
        data class Existing(val taskId: TaskId) : Target

        /**
         * @param title title of the single untimed task the caller creates
         */
        data class Create(val title: String) : Target
    }
}
