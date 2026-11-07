package com.example.clockplannerproject.data

import android.content.Context
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.planner.PlannerPhrases

/**
 * [PlannerPhrases] backed by `values` and `values-ru`.
 *
 * @param context context used to resolve the current locale
 * @since 1.1.0
 */
class AndroidPlannerPhrases(
    private val context: Context,
) : PlannerPhrases {
    override fun serviceUnavailable(): String = context.getString(R.string.planner_error_unavailable)

    override fun emptyGoal(): String = context.getString(R.string.planner_error_empty_goal)

    override fun deadlinePassed(): String = context.getString(R.string.planner_error_deadline_past)

    override fun invalidWindow(): String = context.getString(R.string.planner_error_invalid_window)

    override fun stepDayOutsideDeadline(): String =
        context.getString(R.string.planner_error_step_day)

    override fun fits(freeMinutes: Int, usableMinutes: Int, requiredMinutes: Int): String =
        context.getString(R.string.planner_narrative_fits, freeMinutes, usableMinutes, requiredMinutes)

    override fun tight(freeMinutes: Int, usableMinutes: Int, requiredMinutes: Int): String =
        context.getString(R.string.planner_narrative_tight, freeMinutes, usableMinutes, requiredMinutes)

    override fun impossible(shortageMinutes: Int): String =
        context.getString(R.string.planner_narrative_impossible, shortageMinutes)

    override fun reportNoClosedIntervals(): String =
        context.getString(R.string.planner_report_no_closed)

    override fun reportDone(doneMinutes: Int): String =
        context.getString(R.string.planner_report_done, doneMinutes)

    override fun reportUntimedDone(titles: String): String =
        context.getString(R.string.planner_report_untimed_done, titles)

    override fun reportComparison(plannedMinutes: Int, doneMinutes: Int): String =
        context.getString(R.string.planner_report_compare, plannedMinutes, doneMinutes)

    override fun reportNothingToCompare(): String =
        context.getString(R.string.planner_report_no_compare)

    override fun reportAnchorTitle(isoDate: String): String =
        context.getString(R.string.planner_report_anchor, isoDate)
}
