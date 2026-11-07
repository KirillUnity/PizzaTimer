package com.example.clockplannerproject.kit.core.planner

/**
 * User-facing planner sentences.
 *
 * Production loads these from `values` / `values-ru`. [RussianPlannerPhrases]
 * mirrors `values-ru` for JVM tests. The mock substitutes capacity numbers
 * and does not invent a second set of totals.
 *
 * @since 1.1.0
 */
interface PlannerPhrases {
    /**
     * @return sentence for a service-unavailable refusal
     */
    fun serviceUnavailable(): String

    /**
     * @return sentence for a blank goal
     */
    fun emptyGoal(): String

    /**
     * @return sentence when the deadline is before today
     */
    fun deadlinePassed(): String

    /**
     * @return sentence for an empty or overnight work window
     */
    fun invalidWindow(): String

    /**
     * @return sentence when a step is moved outside today..deadline
     */
    fun stepDayOutsideDeadline(): String

    /**
     * @param freeMinutes free work minutes from capacity
     * @param usableMinutes usable minutes from capacity
     * @param requiredMinutes buffered minutes from capacity
     */
    fun fits(freeMinutes: Int, usableMinutes: Int, requiredMinutes: Int): String

    /**
     * @param freeMinutes free work minutes from capacity
     * @param usableMinutes usable minutes from capacity
     * @param requiredMinutes buffered minutes from capacity
     */
    fun tight(freeMinutes: Int, usableMinutes: Int, requiredMinutes: Int): String

    /**
     * @param shortageMinutes `max(0, requiredMinutes - freeMinutes)` from capacity
     */
    fun impossible(shortageMinutes: Int): String

    /** @return honest sentence for a period with no closed intervals */
    fun reportNoClosedIntervals(): String

    /**
     * @param doneMinutes closed-block total
     */
    fun reportDone(doneMinutes: Int): String

    /**
     * @param titles comma-separated titles of done tasks that have no blocks
     */
    fun reportUntimedDone(titles: String): String

    /**
     * @param plannedMinutes placed minutes from accepted plans in the period
     * @param doneMinutes closed-block total
     */
    fun reportComparison(plannedMinutes: Int, doneMinutes: Int): String

    /** @return sentence used when no accepted plan falls in the period */
    fun reportNothingToCompare(): String

    /**
     * Title of the untimed task created when no goal anchor exists.
     *
     * @param isoDate period end as `yyyy-mm-dd`
     */
    fun reportAnchorTitle(isoDate: String): String
}

/**
 * Russian copy kept in sync with `values-ru/strings.xml`.
 *
 * @since 1.1.0
 */
object RussianPlannerPhrases : PlannerPhrases {
    override fun serviceUnavailable(): String = "Сервис недоступен."

    override fun emptyGoal(): String = "Напишите цель."

    override fun deadlinePassed(): String = "Этот день уже прошёл."

    override fun invalidWindow(): String =
        "Рабочее окно должно быть длиннее нуля и укладываться в одни сутки."

    override fun stepDayOutsideDeadline(): String =
        "Выберите день от сегодня до срока."

    override fun fits(freeMinutes: Int, usableMinutes: Int, requiredMinutes: Int): String =
        "Помещается: свободно $freeMinutes, с запасом $usableMinutes, нужно $requiredMinutes."

    override fun tight(freeMinutes: Int, usableMinutes: Int, requiredMinutes: Int): String =
        "Впритык: свободно $freeMinutes, с запасом $usableMinutes, нужно $requiredMinutes."

    override fun impossible(shortageMinutes: Int): String =
        "Не помещается: не хватает $shortageMinutes мин. " +
            "Можно укоротить цель, сдвинуть день или снять необязательное."

    override fun reportNoClosedIntervals(): String = "Закрытых отрезков нет."

    override fun reportDone(doneMinutes: Int): String = "Закрыто $doneMinutes мин."

    override fun reportUntimedDone(titles: String): String = "Без отрезков: $titles."

    override fun reportComparison(plannedMinutes: Int, doneMinutes: Int): String =
        "В плане стояло $plannedMinutes мин, закрыто $doneMinutes мин."

    override fun reportNothingToCompare(): String = "Сравнивать не с чем."

    override fun reportAnchorTitle(isoDate: String): String = "Отчёт $isoDate"
}
