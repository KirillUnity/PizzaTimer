package com.example.clockplannerproject.kit.core.planner

import com.example.clockplannerproject.kit.core.Importance
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class AcceptedPlanCodecTest {

    @Test
    fun roundTrip_keepsSeparatorsInText() {
        val plan = AcceptedPlan(
            id = "id|1",
            goal = "Цель\nс | чертой",
            deadline = LocalDate(2026, 11, 6),
            steps = listOf(
                PlacedStep(
                    title = "Шаг | 1",
                    date = LocalDate(2026, 11, 6),
                    startMinute = 9 * 60,
                    endMinute = 11 * 60,
                    unplaced = false,
                    estimateMinutes = 120,
                    bufferedMinutes = 144,
                    importance = Importance.MEDIUM,
                    project = "Цель",
                ),
                PlacedStep(
                    title = "Без окна",
                    date = null,
                    startMinute = null,
                    endMinute = null,
                    unplaced = true,
                    estimateMinutes = 150,
                    bufferedMinutes = 180,
                    importance = Importance.HIGH,
                    project = null,
                ),
            ),
        )
        assertEquals(listOf(plan), AcceptedPlanCodec.decode(AcceptedPlanCodec.encode(listOf(plan))))
    }

    @Test
    fun blank_decodesToEmpty() {
        assertEquals(emptyList<AcceptedPlan>(), AcceptedPlanCodec.decode("  "))
    }
}
