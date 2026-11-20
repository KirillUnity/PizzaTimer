package com.example.clockplannerproject.kit.core.practice

/**
 * One craft of the live day. A skill becomes tried only after the matching
 * action. Nothing in this type clears a skill.
 *
 * @since 1.3.0
 */
enum class PracticeSkill {
    /** The user selected a sector while the day was in dial mode. */
    DIAL,

    /** The user saved a task that has no time blocks. */
    LEFTOVER,

    /** Start appended a new interval. */
    START,

    /** The user committed a non-empty pause checkpoint. */
    CHECKPOINT,

    /** The user closed the day from the day-close card. */
    DAY_CLOSE,
}

/**
 * Counts tried skills. The hub reads the same numbers from string resources.
 *
 * @since 1.3.0
 */
object PracticeProgress {
    /** How many crafts the hub lists. */
    const val SKILL_TOTAL: Int = 5

    /**
     * @param tried skills already marked
     * @return how many of [tried] are set, capped only by the set size
     */
    fun triedCount(tried: Set<PracticeSkill>): Int = tried.size

    /**
     * Russian count fragment «N из 5» locked by the unit test.
     *
     * @param count tried skills, usually [triedCount]
     */
    fun triedOfFive(count: Int): String = "$count из $SKILL_TOTAL"
}
