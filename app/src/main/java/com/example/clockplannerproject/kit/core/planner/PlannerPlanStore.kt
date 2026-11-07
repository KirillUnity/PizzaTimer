package com.example.clockplannerproject.kit.core.planner

import kotlinx.coroutines.flow.Flow

/**
 * Device key and accepted plans. Preferences, not a Room table.
 *
 * @since 1.1.0
 */
interface PlannerPlanStore {
    /**
     * Returns the device UUID, creating and saving it on the first call.
     *
     * Callers may read the key. This delivery does not transmit it.
     *
     * @return persisted UUID string
     * @since 1.1.0
     */
    suspend fun deviceKey(): String

    /**
     * Emits the accepted-plan list. Empty until the user places a draft on the dial.
     *
     * @since 1.1.0
     */
    fun observeAcceptedPlans(): Flow<List<AcceptedPlan>>

    /**
     * Current accepted plans.
     *
     * @since 1.1.0
     */
    suspend fun acceptedPlans(): List<AcceptedPlan>

    /**
     * Appends [plan]. The tasks screen calls this only after the user agrees.
     *
     * @param plan goal and the steps that were written to the dial
     * @since 1.1.0
     */
    suspend fun saveAcceptedPlan(plan: AcceptedPlan)
}
