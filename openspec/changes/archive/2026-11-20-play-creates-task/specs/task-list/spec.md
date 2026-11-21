# Spec Delta

## ADDED Requirements

### Requirement: Play without a selection creates one task
When no task is selected and no task on the visible day has an open interval, Play MUST create one task with an empty title and exactly one open interval starting at the current minute. The new task MUST become the selection. A later Play MUST NOT append a second interval and MUST NOT create another task. Closed intervals of other tasks MUST stay unchanged. `DayViewModelTest.playWithoutSelection_createsOneOpenInterval_secondPlayDoesNotAppend` locks this.

#### Scenario: Empty selection starts one interval
- **WHEN** the user presses Play and nothing is selected
- **THEN** the day gains one untitled task with one open interval at the current minute

#### Scenario: Second Play does not append
- **WHEN** the user presses Play again while that interval is still open
- **THEN** the task still has one interval and no second untitled task appears

#### Scenario: An already open task is not duplicated
- **WHEN** some task already has an open interval and nothing is selected
- **THEN** Play does not create a task and does not append an interval
