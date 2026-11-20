package com.example.clockplannerproject.kit.core.practice

import kotlinx.coroutines.flow.Flow

/**
 * Installation-scoped record of which crafts the person has tried.
 *
 * [markTried] only adds. The store has no clear operation: repeating an
 * action stays tried, and Start must not wipe these flags when it clears
 * a pause checkpoint.
 *
 * @since 1.3.0
 */
interface PracticeProgressStore {
    /** Skills marked tried so far. Missing preferences read as an empty set. */
    fun observe(): Flow<Set<PracticeSkill>>

    /**
     * Records [skill] as tried.
     *
     * @param skill the craft the user just performed on a real day
     */
    suspend fun markTried(skill: PracticeSkill)
}
