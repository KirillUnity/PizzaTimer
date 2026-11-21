# Spec Delta

## MODIFIED Requirements

### Requirement: Continue and finish the break
While a break is open, the day MUST offer finish-task and continue. Continue MUST close the break and append exactly one new open work interval on the same task. Finish task MUST close the break, hide those buttons, and MUST open the completion sheet over the dimmed dial. A Play on an interval that is already open MUST NOT append another interval. `DayViewModelTest.continue_appendsOneWorkInterval_playDoesNotDouble` locks continue and the second Play.

#### Scenario: Continue appends one work interval
- **WHEN** the user continues after a pause
- **THEN** the break is closed and the work task has one new open interval after the intervals that were already closed

#### Scenario: Finish hides the break actions
- **WHEN** the user finishes the task from the break
- **THEN** the break is closed, the finish and continue actions are not offered, and the completion sheet is open

#### Scenario: Second Play does not append
- **WHEN** the user presses Play while that new work interval is still open
- **THEN** no further interval is appended

## ADDED Requirements

### Requirement: Completion sheet requires the three proxies
The completion sheet MUST collect state-after, engagement, and skill as integers from 1 to 10, labeled as a self-report and not a clinical scale. It MUST NOT offer an “I am in flow” checkbox and MUST NOT use the words FSS-2. Submitting with any of those three missing MUST leave the sheet open and MUST NOT write a completion. `DayViewModelTest.completion_missingState_staysOpen` locks the refusal.

#### Scenario: Empty state is refused
- **WHEN** the user submits the sheet without a state-after value
- **THEN** the sheet stays open and no completion is stored
