package com.example.clockplannerproject.kit.core.planner

import androidx.compose.runtime.Immutable
import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.Task
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/**
 * Default civil work window: 09:00 inclusive through 19:00 exclusive.
 *
 * @since 1.1.0
 */
const val DEFAULT_WORK_START_MINUTE: Int = 540

/**
 * Exclusive end of the default work window (19:00).
 *
 * @since 1.1.0
 */
const val DEFAULT_WORK_END_MINUTE: Int = 1140

/**
 * Descriptions longer than this are cut before the slice is hashed.
 *
 * @since 1.1.0
 */
const val SLICE_DESCRIPTION_LIMIT: Int = 280

/**
 * One planning request. Replaces `POST /v1/plans` for the on-device mock.
 *
 * [workStartMinute] and [workEndMinute] are a half-open interval inside one
 * local day: `0 <= start < end <= 1440`.
 *
 * @param goal non-blank aim the user wants finished by [deadline]
 * @param deadline local calendar day, not earlier than the device's today
 * @param zone IANA zone the client reports; the mock does not call a server
 * @param workStartMinute inclusive minute of the shared work window
 * @param workEndMinute exclusive minute of the shared work window
 * @param tasks instances and recurrence templates from today through [deadline]
 * @since 1.1.0
 */
@Immutable
data class PlanRequest(
    val goal: String,
    val deadline: LocalDate,
    val zone: TimeZone,
    val workStartMinute: Int = DEFAULT_WORK_START_MINUTE,
    val workEndMinute: Int = DEFAULT_WORK_END_MINUTE,
    val tasks: List<Task> = emptyList(),
)

/**
 * How the estimates sit in the free work windows.
 *
 * @since 1.1.0
 */
enum class PlanVerdict {
    /** Every step has a whole window and the buffered sum fits the usable minutes. */
    FITS,

    /** Every step fits, but only at the raw estimate; the buffer no longer fits. */
    TIGHT,

    /** The minute sum is short, or at least one step has no single free window. */
    IMPOSSIBLE,
}

/**
 * Model estimate before placement. The mock fills this from a fixed table.
 *
 * @param title step title shown on the draft
 * @param estimateMinutes raw length in minutes, at least 1 when the table is valid
 * @param bufferedMinutes `ceil(estimateMinutes * 1.2)`, the length used for [PlanVerdict.FITS]
 * @param importance stored on the step; timed steps do not use leftover weights
 * @param project project tab, normally the goal title
 * @since 1.1.0
 */
@Immutable
data class StepEstimate(
    val title: String,
    val estimateMinutes: Int,
    val bufferedMinutes: Int,
    val importance: Importance,
    val project: String?,
)

/**
 * One draft step after greedy placement.
 *
 * A placed step has [date], [startMinute], and [endMinute]. An unplaced step
 * has [unplaced] set and null civil fields. The piece is never split.
 *
 * @param title step title
 * @param date local day of the piece, or null when [unplaced]
 * @param startMinute inclusive start, or null when [unplaced]
 * @param endMinute exclusive end, or null when [unplaced]
 * @param unplaced true when no single free window could hold the piece
 * @param estimateMinutes raw estimate from the table or model
 * @param bufferedMinutes `ceil(estimateMinutes * 1.2)`
 * @param importance importance copied from the estimate
 * @param project project copied from the estimate
 * @since 1.1.0
 */
@Immutable
data class PlacedStep(
    val title: String,
    val date: LocalDate?,
    val startMinute: Int?,
    val endMinute: Int?,
    val unplaced: Boolean,
    val estimateMinutes: Int,
    val bufferedMinutes: Int,
    val importance: Importance,
    val project: String?,
) {
    /** Exclusive length of a placed piece, or 0 when [unplaced]. */
    val placedMinutes: Int
        get() {
            val start = startMinute ?: return 0
            val end = endMinute ?: return 0
            return (end - start).coerceAtLeast(0)
        }
}

/**
 * Capacity figures plus the placed rows. Narrative is added by the source, not here.
 *
 * @param verdict [PlanVerdict] from the fit rules
 * @param freeMinutes sum of free work minutes across the horizon
 * @param usableMinutes sum of `floor(freeDay * 4 / 5)`
 * @param requiredMinutes sum of buffered step lengths
 * @param steps one row per input estimate, in the original estimate order
 * @since 1.1.0
 */
@Immutable
data class CapacityResult(
    val verdict: PlanVerdict,
    val freeMinutes: Int,
    val usableMinutes: Int,
    val requiredMinutes: Int,
    val steps: List<PlacedStep>,
)

/**
 * Successful plan draft kept in process memory for an identical request.
 *
 * @param draftId stable id for this canonical request inside the process
 * @param verdict fit verdict
 * @param freeMinutes minutes left in the work windows
 * @param usableMinutes minutes left after the one-fifth buffer
 * @param requiredMinutes buffered minutes the steps need
 * @param steps placed and unplaced rows
 * @param narrative short explanation; numbers come only from [CapacityResult]
 * @since 1.1.0
 */
@Immutable
data class PlanDraft(
    val draftId: String,
    val verdict: PlanVerdict,
    val freeMinutes: Int,
    val usableMinutes: Int,
    val requiredMinutes: Int,
    val steps: List<PlacedStep>,
    val narrative: String,
)

/**
 * Why [com.example.clockplannerproject.kit.core.planner.PlannerSource.plan] returned no draft.
 *
 * @since 1.1.0
 */
sealed interface PlanRejection {
    /** User-facing sentence loaded from resources (or the JVM mirror). */
    val message: String

    /**
     * Goal was blank after trim.
     *
     * @param message localized explanation
     */
    data class EmptyGoal(override val message: String) : PlanRejection

    /**
     * Deadline is strictly before the device-local today.
     *
     * @param message localized explanation
     */
    data class DeadlineBeforeToday(override val message: String) : PlanRejection

    /**
     * Work window is empty, reversed, or outside `0..1440`.
     *
     * @param message localized explanation
     */
    data class InvalidWindow(override val message: String) : PlanRejection

    /**
     * Mock stand-in for an unreachable service. No draft is stored.
     *
     * @param message localized explanation
     */
    data class Unavailable(override val message: String) : PlanRejection
}

/**
 * Result of a plan request. [Rejected] carries no draft id and no steps.
 *
 * @since 1.1.0
 */
sealed interface PlanOutcome {
    /**
     * @param draft cached or freshly placed plan
     */
    data class Ready(val draft: PlanDraft) : PlanOutcome

    /**
     * @param reason validation or availability failure
     */
    data class Rejected(val reason: PlanRejection) : PlanOutcome
}
