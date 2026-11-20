package com.example.clockplannerproject.ui.task

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Release builds hide the mock-word hint. The mock still honors those words if typed.
 *
 * @param modifier unused in release; kept so the call site compiles
 * @since 1.1.0
 */
@Composable
internal fun PlannerMockHint(modifier: Modifier = Modifier) {
    // No tester copy in release.
}
