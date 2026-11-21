# Spec Delta

## MODIFIED Requirements

### Requirement: Play without a selection creates one task
When no task is selected and no task on the visible day has an open interval, Play MUST create one task whose title is the visible date and the interval start minute formatted `dd.MM.yyyy HH:mm`, and exactly one open interval starting at the current minute. The new task MUST become the selection. A later Play MUST NOT append a second interval and MUST NOT create another task. Play on an already selected task MUST NOT change its title. Closed intervals of other tasks MUST stay unchanged. `DayViewModelTest.playWithoutSelection_createsOneOpenInterval_secondPlayDoesNotAppend` locks this.

#### Scenario: Empty selection starts one interval
- **WHEN** the user presses Play and nothing is selected on 20 November 2026 at 17:58
- **THEN** the day gains one task titled `20.11.2026 17:58` with one open interval at that minute, and that task is selected

#### Scenario: Second Play does not append
- **WHEN** the user presses Play again while that interval is still open
- **THEN** the task still has one interval and no second task appears

#### Scenario: An already open task is not duplicated
- **WHEN** some task already has an open interval and nothing is selected
- **THEN** Play does not create a task and does not append an interval

#### Scenario: Selected task keeps its name
- **WHEN** a task with a stored title is selected and the user starts it
- **THEN** the title stays as stored

### Requirement: Name and next task
The sheet MUST ask for a name when the task title is empty or equals the provisional `dd.MM.yyyy HH:mm` title for that task's date and earliest interval start, and MUST show any other existing title read-only. The provisional field MUST be prefilled with that title. A non-blank replacement MUST become the stored title. A blank name MUST NOT save. The sheet MUST NOT show a next-task field. A blank next-task name MUST NOT create a task. A non-blank next-task name in saved state MUST still create one separate task with no open interval. `DayViewModelTest.completion_namesTask_andAddsNextWithoutOpenInterval` locks the save path.

#### Scenario: Untitled task gains a name and a next task
- **WHEN** the user names an untitled task, fills the three proxies and one demand mark, and types a next-task name
- **THEN** the task carries the typed name and a separate task with that next name exists with no open interval

#### Scenario: Provisional title can be replaced
- **WHEN** the user finishes a task titled `20.11.2026 17:58` that started at 17:58 on that date, replaces the name with `Черновик`, and saves the completion
- **THEN** the task title is `Черновик`

#### Scenario: A written title stays read-only
- **WHEN** the user opens the completion sheet for a task whose title is not empty and is not that provisional stamp
- **THEN** the sheet shows the stored title and does not offer a name field
