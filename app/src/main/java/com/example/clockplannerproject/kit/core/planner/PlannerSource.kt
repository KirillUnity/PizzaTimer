package com.example.clockplannerproject.kit.core.planner

/**
 * On-device stand-in for `POST /v1/plans` and `POST /v1/reports`.
 *
 * The only production binding is the mock. It must not open a socket.
 *
 * @since 1.1.0
 */
interface PlannerSource {
    /**
     * Places a goal or refuses before placement.
     *
     * An empty goal, a deadline before local today, or a zero-length window
     * returns [PlanOutcome.Rejected] and stores no draft. The same canonical
     * request (goal, deadline, window, zone, slice hash) returns the same draft.
     *
     * @param request goal, horizon, work window, and task slice
     * @return a draft or a refusal with no steps
     * @since 1.1.0
     */
    suspend fun plan(request: PlanRequest): PlanOutcome

    /**
     * Builds a local report paragraph. Does not write tasks or notes.
     *
     * A service-unavailable refusal returns [ReportOutcome.Rejected] and no
     * paragraph. The caller leaves existing notes and tasks untouched.
     *
     * @param request period, tasks, and accepted plans supplied by the caller
     * @return a paragraph, or a refusal with no text
     * @since 1.1.0
     */
    suspend fun report(request: ReportRequest): ReportOutcome
}
