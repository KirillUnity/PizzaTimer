# Spec Delta

## ADDED Requirements

### Requirement: Pause starts a break
Pause MUST close the open work interval at the current minute and MUST start one break wedge from that minute. Pause MUST NOT open the stop-note slip. Already ended intervals MUST stay unchanged. The break MUST be visible on the dial. `DayViewModelTest.pause_startsBreak_andLeavesClosedBlocks` locks this.

#### Scenario: Pause leaves the old interval and skips the note
- **WHEN** the user pauses a running task that already has an ended interval
- **THEN** that ended interval is unchanged, the open interval is closed, a break starts at the current minute, and the stop-note slip stays closed

### Requirement: Continue and finish the break
While a break is open, the day MUST offer finish-task and continue. Continue MUST close the break and append exactly one new open work interval on the same task. Finish task MUST close the break, hide those buttons, and MUST NOT open a ratings sheet. A Play on an interval that is already open MUST NOT append another interval. `DayViewModelTest.continue_appendsOneWorkInterval_playDoesNotDouble` locks continue and the second Play.

#### Scenario: Continue appends one work interval
- **WHEN** the user continues after a pause
- **THEN** the break is closed and the work task has one new open interval after the intervals that were already closed

#### Scenario: Finish hides the break actions
- **WHEN** the user finishes the task from the break
- **THEN** the break is closed and the finish and continue actions are not offered

#### Scenario: Second Play does not append
- **WHEN** the user presses Play while that new work interval is still open
- **THEN** no further interval is appended
