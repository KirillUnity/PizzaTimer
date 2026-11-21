## ADDED Requirements

### Requirement: Break wedges stay off the task list
The tasks tab MUST NOT list pause-break rows and MUST NOT offer the break tag as a filter chip. `TasksViewModelTest.visibleTasks_hidesBreakWedges` locks this.

#### Scenario: Four sand rows
- **WHEN** the store has work plus break wedges
- **THEN** the visible list is only the work task

### Requirement: Periods on one day do not overlap
Saving a timed task MUST fail when any of its periods overlap another non-break task on that date or overlap each other. Untimed tasks MAY share leftover. `TaskDraftValidatorTest.overlappingSameDay_isError` locks this.

#### Scenario: Second task on the same hours
- **WHEN** the user saves 09:00–10:00 and another task on that day already occupies 09:00–10:00
- **THEN** the editor stays open with an overlapping-period error and the second task is not stored

### Requirement: Unclosed periods can be edited and removed
An interval that has not ended MUST be removable and MUST accept a new start and end. An already-ended interval MUST stay locked. `TasksViewModelTest.removeBlock_dropsUnclosed_keepsClosed` locks remove.

#### Scenario: Remove an open segment
- **WHEN** the user removes an unclosed interval
- **THEN** that interval is gone and closed intervals on the same task stay
