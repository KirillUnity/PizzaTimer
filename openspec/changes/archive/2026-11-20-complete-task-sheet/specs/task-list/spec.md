# Spec Delta

## ADDED Requirements

### Requirement: Completion lives on the task
A saved completion MUST attach to the task and MUST NOT be stored inside a closed TimeBlock. Saving MUST leave already-ended intervals unchanged. `TaskCompletionTest` locks the codec, including rejection when state is missing.

#### Scenario: Closed intervals stay put
- **WHEN** the user saves a completion
- **THEN** every already-ended interval on that task is unchanged

### Requirement: Name and next task
The sheet MUST ask for a name when the task title is empty and MUST show an existing title read-only. A blank next-task name MUST NOT create a task. A non-blank next-task name MUST create one separate task with no open interval. `DayViewModelTest.completion_namesTask_andAddsNextWithoutOpenInterval` locks this.

#### Scenario: Untitled task gains a name and a next task
- **WHEN** the user names an untitled task, fills the three proxies and one demand mark, and types a next-task name
- **THEN** the task carries the typed name and a separate task with that next name exists with no open interval

### Requirement: One demand mark
A completion MUST record exactly one of challenge or hindrance.

#### Scenario: Challenge and hindrance stay exclusive
- **WHEN** a completion is stored
- **THEN** it records challenge or hindrance, not both
