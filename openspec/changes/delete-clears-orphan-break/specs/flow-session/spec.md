## ADDED Requirements

### Requirement: Deleting work clears its break
Deleting a work task MUST also delete every break wedge that points at that task. After that delete the day MUST NOT show Finish task. `DayViewModelTest.deleteWorkWhilePaused_removesBreak` locks this.

#### Scenario: Delete during a paused Play
- **WHEN** a break is open and the user confirms delete of the work task
- **THEN** the break row is gone and Finish task is not offered
