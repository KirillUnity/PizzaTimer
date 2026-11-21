package com.example.clockplannerproject.kit.core

/**
 * Short Flow State Scale-2 and Short Dispositional Flow Scale-2.
 *
 * Each published short form has nine items, one per flow dimension, scored
 * from 1 (strongly disagree) to 5 (strongly agree).
 *
 * Source: Jackson, S. A., Martin, A. J., & Eklund, R. C. (2008). Long and short
 * measures of flow: The construct validity of the FSS-2, DFS-2, and new brief
 * counterparts. Journal of Sport and Exercise Psychology, 30(5), 561–587.
 *
 * [DIMENSIONS] names those nine dimensions. The strings are paraphrases of the
 * dimension labels. They are not the copyrighted questionnaire sentences.
 *
 * A stored profile is nine integers. It is not a diagnosis and it does not
 * mean the user is in flow. The 1–10 proxies stay a separate record.
 *
 * @since 1.2.0
 */
object ShortFlowScale {
    /** Published item count for the short state and dispositional forms. */
    const val ITEM_COUNT: Int = 9

    /** Inclusive published anchors: strongly disagree through strongly agree. */
    val ANCHORS: IntRange = 1..5

    /**
     * Dimension order used by the short forms. Not the published item wording.
     */
    val DIMENSIONS: List<String> = listOf(
        "challenge-skill",
        "action-awareness",
        "clear-goals",
        "unambiguous-feedback",
        "concentration",
        "sense-of-control",
        "loss-of-self-consciousness",
        "time-transformation",
        "autotelic",
    )

    /**
     * @return the nine answers when every one sits on [ANCHORS], otherwise null
     */
    fun profileOrNull(answers: List<Int>?): List<Int>? {
        if (answers == null || answers.size != ITEM_COUNT) return null
        if (answers.any { it !in ANCHORS }) return null
        return answers
    }

    /**
     * Mean of each dimension across [profiles]. Empty when nobody answered.
     * Not added into the 1–10 skill mean.
     */
    fun means(profiles: List<List<Int>>): List<Double> {
        val complete = profiles.filter { profileOrNull(it) != null }
        if (complete.isEmpty()) return emptyList()
        return (0 until ITEM_COUNT).map { index ->
            complete.map { it[index] }.average()
        }
    }
}
