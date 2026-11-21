## MODIFIED Requirements

### Requirement: Continue and finish the break
While a break is open, the day MUST show one centered filled button to finish the task and MUST NOT show Continue on the face. Continue MUST still close the break and append exactly one new open work interval on the same task when Play is pressed on the paused hole. Finish task MUST close the break, hide that button, and MUST open the completion sheet. A Play on an interval that is already open MUST NOT append another interval. `DayViewModelTest.continue_appendsOneWorkInterval_playDoesNotDouble` locks continue and the second Play.

#### Scenario: Continue appends one work interval
- **WHEN** the user continues after a pause
- **THEN** the break is closed and the work task has one new open interval after the intervals that were already closed

#### Scenario: Finish hides the break actions
- **WHEN** the user finishes the task from the break
- **THEN** the break is closed, the finish and continue actions are not offered, and the completion sheet is open

#### Scenario: Second Play does not append
- **WHEN** the user presses Play while that new work interval is still open
- **THEN** no further interval is appended
