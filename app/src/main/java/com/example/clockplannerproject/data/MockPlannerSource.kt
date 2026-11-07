package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.TimeProvider
import com.example.clockplannerproject.kit.core.planner.Capacity
import com.example.clockplannerproject.kit.core.planner.PlanDraft
import com.example.clockplannerproject.kit.core.planner.PlanOutcome
import com.example.clockplannerproject.kit.core.planner.PlanRejection
import com.example.clockplannerproject.kit.core.planner.PlanRequest
import com.example.clockplannerproject.kit.core.planner.PlanVerdict
import com.example.clockplannerproject.kit.core.planner.PlannerPhrases
import com.example.clockplannerproject.kit.core.planner.PlannerPlanStore
import com.example.clockplannerproject.kit.core.planner.PlannerSource
import com.example.clockplannerproject.kit.core.planner.ReportOutcome
import com.example.clockplannerproject.kit.core.planner.ReportRejection
import com.example.clockplannerproject.kit.core.planner.ReportRequest
import com.example.clockplannerproject.kit.core.planner.ReportSummary
import com.example.clockplannerproject.kit.core.planner.SliceHash
import com.example.clockplannerproject.kit.core.planner.StepEstimate
import com.example.clockplannerproject.kit.core.time.TimeMath
import java.util.UUID
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-process planner. Step lengths come from a fixed table; free windows and
 * the verdict come from [Capacity]. Drafts live in process memory.
 *
 * The device key is read on every [plan] and [report] and is not attached to
 * the result. Nothing is sent off the device. Tasks are not written.
 *
 * @param timeProvider local today and the minute that closes an open timer
 * @param planStore device key store; accepted plans are not written here
 * @param phrases localized sentences, with capacity numbers substituted in
 * @param newDraftId id factory for the first response of a canonical request
 * @since 1.1.0
 */
class MockPlannerSource(
    private val timeProvider: TimeProvider,
    private val planStore: PlannerPlanStore,
    private val phrases: PlannerPhrases,
    private val newDraftId: () -> String = { UUID.randomUUID().toString() },
) : PlannerSource {
    private val lock = Mutex()
    private val drafts = LinkedHashMap<String, PlanDraft>()

    override suspend fun plan(request: PlanRequest): PlanOutcome = lock.withLock {
        readDeviceKey()
        val goal = request.goal.trim()
        rejection(goal, request)?.let { return PlanOutcome.Rejected(it) }
        if (isOfflineGoal(goal)) {
            return PlanOutcome.Rejected(PlanRejection.Unavailable(phrases.serviceUnavailable()))
        }
        val key = cacheKey(goal, request)
        drafts[key]?.let { return PlanOutcome.Ready(it) }
        val estimates = estimatesFor(goal)
        val now = timeProvider.now()
        val placed = Capacity.place(
            today = timeProvider.today(),
            deadline = request.deadline,
            nowMinute = now.hour * MINUTES_PER_HOUR + now.minute,
            workStartMinute = request.workStartMinute,
            workEndMinute = request.workEndMinute,
            tasks = request.tasks,
            steps = estimates,
        )
        val draft = PlanDraft(
            draftId = newDraftId(),
            verdict = placed.verdict,
            freeMinutes = placed.freeMinutes,
            usableMinutes = placed.usableMinutes,
            requiredMinutes = placed.requiredMinutes,
            steps = placed.steps,
            narrative = narrative(placed.verdict, placed.freeMinutes, placed.usableMinutes, placed.requiredMinutes),
        )
        drafts[key] = draft
        PlanOutcome.Ready(draft)
    }

    override suspend fun report(request: ReportRequest): ReportOutcome {
        readDeviceKey()
        if (isOfflineSlice(request)) {
            return ReportOutcome.Rejected(ReportRejection.Unavailable(phrases.serviceUnavailable()))
        }
        return ReportOutcome.Ready(ReportSummary.build(request, phrases))
    }

    private fun isOfflineSlice(request: ReportRequest): Boolean =
        request.acceptedPlans.any { plan -> isOfflineGoal(plan.goal.trim()) } ||
            request.tasks.any { task -> isOfflineGoal(task.title.trim()) }

    private suspend fun readDeviceKey() {
        check(planStore.deviceKey().isNotEmpty())
    }

    private fun rejection(goal: String, request: PlanRequest): PlanRejection? {
        if (goal.isEmpty()) return PlanRejection.EmptyGoal(phrases.emptyGoal())
        if (request.deadline < timeProvider.today()) {
            return PlanRejection.DeadlineBeforeToday(phrases.deadlinePassed())
        }
        if (!isValidWindow(request.workStartMinute, request.workEndMinute)) {
            return PlanRejection.InvalidWindow(phrases.invalidWindow())
        }
        return null
    }

    private fun narrative(
        verdict: PlanVerdict,
        freeMinutes: Int,
        usableMinutes: Int,
        requiredMinutes: Int,
    ): String = when (verdict) {
        PlanVerdict.FITS -> phrases.fits(freeMinutes, usableMinutes, requiredMinutes)
        PlanVerdict.TIGHT -> phrases.tight(freeMinutes, usableMinutes, requiredMinutes)
        PlanVerdict.IMPOSSIBLE -> phrases.impossible((requiredMinutes - freeMinutes).coerceAtLeast(0))
    }

    private fun estimatesFor(goal: String): List<StepEstimate> {
        val rows = if (DOZENS_MARKER in goal.lowercase()) {
            List(DOZENS_STEP_COUNT) { index ->
                "$goal: ${index + 1}" to DOZENS_ESTIMATE_MINUTES
            }
        } else {
            listOf(
                "$goal: собрать" to SHORT_ESTIMATE_MINUTES,
                "$goal: черновик" to MEDIUM_ESTIMATE_MINUTES,
                "$goal: вычитка" to SHORT_ESTIMATE_MINUTES,
            )
        }
        return rows.map { (title, estimate) ->
            StepEstimate(
                title = title,
                estimateMinutes = estimate,
                bufferedMinutes = Capacity.bufferedMinutesOf(estimate),
                importance = Importance.MEDIUM,
                project = goal,
            )
        }
    }

    private fun cacheKey(goal: String, request: PlanRequest): String = listOf(
        goal,
        request.deadline.toString(),
        request.zone.id,
        request.workStartMinute.toString(),
        request.workEndMinute.toString(),
        SliceHash.of(request.tasks),
    ).joinToString(separator = "\u001f")

    private companion object {
        const val MINUTES_PER_HOUR: Int = 60
        const val OFFLINE_PREFIX: String = "офлайн"
        const val DOZENS_MARKER: String = "десятки"
        const val DOZENS_STEP_COUNT: Int = 4
        const val DOZENS_ESTIMATE_MINUTES: Int = 600
        const val SHORT_ESTIMATE_MINUTES: Int = 120
        const val MEDIUM_ESTIMATE_MINUTES: Int = 150

        fun isOfflineGoal(goal: String): Boolean =
            goal.lowercase().startsWith(OFFLINE_PREFIX)

        fun isValidWindow(start: Int, end: Int): Boolean =
            start >= 0 && end <= TimeMath.MINUTES_PER_DAY && start < end
    }
}
