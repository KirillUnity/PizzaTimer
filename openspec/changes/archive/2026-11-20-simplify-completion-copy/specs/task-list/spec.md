## MODIFIED Requirements

### Requirement: Name and next task
The sheet MUST ask for a name when the task title is empty and MUST show an existing title read-only. The sheet MUST NOT show a next-task field. A blank next-task name MUST NOT create a task. A non-blank next-task name in saved state MUST still create one separate task with no open interval. `DayViewModelTest.completion_namesTask_andAddsNextWithoutOpenInterval` locks the save path.

#### Scenario: Untitled task gains a name and a next task
- **WHEN** the user names an untitled task, fills the three proxies and one demand mark, and types a next-task name
- **THEN** the task carries the typed name and a separate task with that next name exists with no open interval
