package com.example.clockplannerproject.kit.core.layout

import com.example.clockplannerproject.kit.core.Task

/**
 * Integer leftover minutes for untimed tasks. UI must not recompute shares.
 *
 * Equal [com.example.clockplannerproject.kit.core.Importance] yields equal
 * minutes. Distinct HIGH/MEDIUM/LOW on a full leftover pool is 50/30/20
 * (last slice absorbs the remainder).
 *
 * @since 0.3.0
 */
object LeftoverShares {
    /**
     * @param untimed tasks with empty [Task.blocks]
     * @param leftoverMinutes free minutes on one 12-hour face
     * @return each task paired with minutes to paint; empty if there is no pool
     */
    fun splitMinutes(untimed: List<Task>, leftoverMinutes: Int): List<Pair<Task, Int>> {
        if (untimed.isEmpty() || leftoverMinutes <= 0) return emptyList()
        val weights = untimed.map { it.importance.leftoverWeight }
        val minutes = integerShares(weights, leftoverMinutes)
        return untimed.zip(minutes)
    }

    /**
     * Proportional integer split. The last index receives whatever is left
     * so the sum always equals [total].
     *
     * @since 0.3.0
     */
    fun integerShares(weights: List<Int>, total: Int): List<Int> {
        if (weights.isEmpty() || total <= 0) return weights.map { 0 }
        val weightSum = weights.sum()
        if (weightSum <= 0) return List(weights.size) { 0 }
        var remaining = total
        return weights.mapIndexed { index, weight ->
            if (index == weights.lastIndex) {
                remaining
            } else {
                val share = (weight.toLong() * total / weightSum).toInt()
                remaining -= share
                share
            }
        }
    }
}
