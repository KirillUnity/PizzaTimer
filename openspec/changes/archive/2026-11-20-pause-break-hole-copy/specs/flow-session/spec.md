## ADDED Requirements

### Requirement: Pause keeps the work hole
Pause MUST keep the work task selected. The hole MUST show that task’s name and minutes left when a budget exists. The caption MUST be the break title and MUST NOT be «этот заход». Pause MUST NOT show IN FLOW or elapsed on that hole. `DayViewModelTest.pause_whileBreakOpen_keepsWorkSelected` locks the selection.

#### Scenario: Pause copy is the break title
- **WHEN** the user pauses a running work interval
- **THEN** the selected task is the work task, not the break wedge

#### Scenario: Pause after the work sector is already selected
- **WHEN** a break is open and Pause is pressed again
- **THEN** the work task stays selected and its closed intervals stay unchanged

## MODIFIED Requirements

### Requirement: The session hole shows minutes left
While a work interval is open, the hole MUST show the task name and one quiet line of minutes left. It MUST NOT show a percent, a red ring, ratings, or the day’s done total. While a break is open and the work task is selected, the hole MUST keep that minutes-left line and MUST use the break caption instead of the this-session caption.

#### Scenario: Open session hides the day total
- **WHEN** a task has an open interval
- **THEN** the day face does not show the done-today line and the hole shows minutes left

### Requirement: Continue and finish the break
While a break is open, the day MUST NOT show finish-task and continue above the dial. Continue MUST still close the break and append exactly one new open work interval on the same task when Play is pressed on the paused hole. Finish task MUST still close the break and MUST open the completion sheet. A Play on an interval that is already open MUST NOT append another interval. `DayViewModelTest.continue_appendsOneWorkInterval_playDoesNotDouble` locks continue and the second Play.

#### Scenario: Continue appends one work interval
- **WHEN** the user continues after a pause
- **THEN** the break is closed and the work task has one new open interval after the intervals that were already closed

#### Scenario: Finish hides the break actions
- **WHEN** the user finishes the task from the break
- **THEN** the break is closed, the finish and continue actions are not offered, and the completion sheet is open

#### Scenario: Second Play does not append
- **WHEN** the user presses Play while that new work interval is still open
- **THEN** no further interval is appended
