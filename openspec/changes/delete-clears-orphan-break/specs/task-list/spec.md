## MODIFIED Requirements

### Requirement: Confirm removes the task and its scores
Confirm MUST remove that task, its intervals, its scores, and any break wedge that points at it. Other tasks and their intervals MUST stay unchanged. `DayViewModelTest.confirmDelete_removesTaskIntervalsAndScores` and `DayViewModelTest.deleteWorkWhilePaused_removesBreak` lock this.

#### Scenario: Two intervals and a neighbor
- **WHEN** the user confirms delete for a task that has two intervals and scores, and another task is on the same day
- **THEN** the deleted task and its scores are gone and the neighbor’s intervals are unchanged
