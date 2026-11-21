# Spec Delta

## ADDED Requirements

### Requirement: Delete asks with the paper dialog
The day MUST ask before deleting a task. The dialog title MUST be «Удалить это дело?». The body MUST say the task and its intervals disappear from this day. «Оставить» MUST be the terracotta action and MUST delete nothing. «Удалить» MUST be plain text and MUST NOT use a red fill. `DayViewModelTest.leaveDelete_keepsTaskAndScores` locks the keep action.

#### Scenario: Leave keeps the task
- **WHEN** the user chooses leave
- **THEN** the task, its intervals, and its scores are still stored

### Requirement: Confirm removes the task and its scores
Confirm MUST remove that task, its intervals, and its scores from the device. Other tasks and their intervals MUST stay unchanged. `DayViewModelTest.confirmDelete_removesTaskIntervalsAndScores` locks this.

#### Scenario: Two intervals and a neighbor
- **WHEN** the user confirms delete for a task that has two intervals and scores, and another task is on the same day
- **THEN** the deleted task and its scores are gone and the neighbor’s intervals are unchanged
