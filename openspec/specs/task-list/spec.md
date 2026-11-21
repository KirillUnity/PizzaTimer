# task-list Specification

## Purpose
Preserve how the day's tasks are planned, started, and paused so later work cannot rewrite closed time.

## Requirements

### Requirement: Closed intervals stay unchanged
The system MUST keep every already-ended interval as stored when the user starts or pauses a task.

#### Scenario: Pause closes only the open interval
- **WHEN** the user pauses a task that has older ended intervals and one open interval
- **THEN** the older intervals stay as stored and only the open interval receives an end

#### Scenario: Start appends a new interval
- **WHEN** the user starts a task that has no open interval
- **THEN** the system appends a new open interval and leaves every existing interval unchanged

#### Scenario: Start is ignored while running
- **WHEN** the user starts a task that already has an open interval
- **THEN** the task's intervals stay unchanged

### Requirement: Untimed work shares leftover minutes
The system SHALL give untimed tasks the minutes not taken by timed tasks, split by importance high:medium:low as 5:3:2. Timed tasks keep their civil start and end.

#### Scenario: Higher importance takes a larger share
- **WHEN** leftover minutes are shared by untimed tasks of different importance
- **THEN** a high-importance task receives a larger share than a medium one, and a medium one a larger share than a low one, in the ratio 5:3:2

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

### Requirement: Completion lives on the task
A saved completion MUST attach to the task and MUST NOT be stored inside a closed TimeBlock. Saving MUST leave already-ended intervals unchanged. `TaskCompletionTest` locks the codec, including rejection when state is missing.

#### Scenario: Closed intervals stay put
- **WHEN** the user saves a completion
- **THEN** every already-ended interval on that task is unchanged

### Requirement: Name and next task
The sheet MUST ask for a name when the task title is empty and MUST show an existing title read-only. The sheet MUST NOT show a next-task field. A blank next-task name MUST NOT create a task. A non-blank next-task name in saved state MUST still create one separate task with no open interval. `DayViewModelTest.completion_namesTask_andAddsNextWithoutOpenInterval` locks the save path.

#### Scenario: Untitled task gains a name and a next task
- **WHEN** the user names an untitled task, fills the three proxies and one demand mark, and types a next-task name
- **THEN** the task carries the typed name and a separate task with that next name exists with no open interval

### Requirement: One demand mark
A completion MUST record exactly one of challenge or hindrance.

#### Scenario: Challenge and hindrance stay exclusive
- **WHEN** a completion is stored
- **THEN** it records challenge or hindrance, not both

### Requirement: Delete asks with the paper dialog
The day MUST ask before deleting a task. The dialog title MUST be «Удалить это дело?». The body MUST say the task and its intervals disappear from this day. «Оставить» MUST be the terracotta action and MUST delete nothing. «Удалить» MUST be plain text and MUST NOT use a red fill. `DayViewModelTest.leaveDelete_keepsTaskAndScores` locks the keep action.

#### Scenario: Leave keeps the task
- **WHEN** the user chooses leave
- **THEN** the task, its intervals, and its scores are still stored

### Requirement: Several tasks can be selected for delete
The tasks tab MUST let the user mark more than one visible task and MUST ask before removing them. A long press MUST start selection on that row. While any row is marked, a tap MUST toggle membership instead of opening the editor. Confirm MUST delete each selected task, its intervals, and break wedges that point at it. Tasks that were not selected MUST stay. Break wedges MUST NOT appear as selectable rows. `TasksViewModelTest.confirmDeleteSelected_removesMarked_keepsOthers` locks this.

#### Scenario: Two marked, one neighbor
- **WHEN** the user confirms delete for two selected tasks while a third task is on the same day
- **THEN** the two are gone and the third remains

#### Scenario: Leave keeps the marked set
- **WHEN** the user dismisses the bulk delete dialog
- **THEN** every selected task is still stored

### Requirement: Confirm removes the task and its scores
Confirm MUST remove that task, its intervals, its scores, and any break wedge that points at it. Other tasks and their intervals MUST stay unchanged. `DayViewModelTest.confirmDelete_removesTaskIntervalsAndScores` and `DayViewModelTest.deleteWorkWhilePaused_removesBreak` lock this.

#### Scenario: Two intervals and a neighbor
- **WHEN** the user confirms delete for a task that has two intervals and scores, and another task is on the same day
- **THEN** the deleted task and its scores are gone and the neighbor’s intervals are unchanged

### Requirement: A task may link to an intermediate goal
A task MUST save when it has no intermediate goal. When it has one, the next task created from the completion sheet MUST inherit that same intermediate goal. `GoalBookTest.unlinkedTask_andInheritedNext` locks the link. `DayViewModelTest.completion_nextTaskInheritsIntermediate` locks the sheet.

#### Scenario: No intermediate goal
- **WHEN** a task is saved with no intermediate goal
- **THEN** the task is stored and the link map has no entry for it

#### Scenario: Next task inherits
- **WHEN** the completion sheet creates a next task and the current task is linked to an intermediate goal
- **THEN** the next task uses that same intermediate goal

### Requirement: A habitual task can be nudged once
When the last three completions of tasks with the same title each have skill at least 8 and engagement at most 4, the system MUST offer once to harden the next similar task by one of: already a step, an explicit requirement, or a narrower result. Two completions MUST NOT offer it. Skipping MUST NOT ask about those same three again. Agreeing MUST NOT change closed intervals or the saved scores. `DifficultyNudgeTest.threshold_and_skip` locks this.

#### Scenario: Two completions stay quiet
- **WHEN** only two similar completions meet the skill and engagement threshold
- **THEN** no offer is shown

#### Scenario: Skip hides the same three
- **WHEN** the user skips the offer for three qualifying completions
- **THEN** those same three do not raise the offer again

#### Scenario: Agree leaves intervals and scores
- **WHEN** the user agrees to one hardening
- **THEN** closed intervals and the 1–10 scores stay as they were

### Requirement: Tasks header rows do not overlap
The tasks screen MUST place the date strip, the plan-a-goal entry, and the project filters in a vertical column. Those rows MUST NOT be stacked in the same grid cell.

#### Scenario: Planner card sits below the date strip
- **WHEN** the tasks tab is shown
- **THEN** the plan-a-goal title is fully inside its card and does not share pixels with the date-strip title

### Requirement: New task starts with a chooser
Opening create MUST NOT dump the full editor. The first surface MUST include a title field, creating an untimed task on the selected day in one action, and a configure path that uses skippable stages. `TasksViewModelTest.createTodayQuick_savesUntimedOnSelectedDay` and `TasksViewModelTest.createTodayQuick_usesTypedTitle` lock the one-tap path.

#### Scenario: One tap puts work on today
- **WHEN** the user chooses create-for-today and the title is empty
- **THEN** an untimed task is saved on the selected day with the default new-task name and the editor closes

#### Scenario: Typed name is kept
- **WHEN** the user types a title on the chooser and chooses create-for-today
- **THEN** the stored task uses that title

### Requirement: Configure path is skippable stages
The configure path MUST show name, when, look, and details as separate stages. Each stage MUST explain why it exists. Skip MUST advance without requiring that stage’s fields. Skip or Next on the last stage MUST save, filling the default name if the title is still blank. `TasksViewModelTest.createWizard_skipAll_savesDefaultName` locks a full skip.

#### Scenario: Skip never blocks save
- **WHEN** the user starts configure and skips every stage
- **THEN** a task is stored with the default name and default look, with no intervals

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

### Requirement: Edit uses expandable sections
Editing an existing task MUST NOT dump every field at once. The sheet MUST list name, when, look, and details as collapsed rows. Tapping a row MUST expand or collapse that section. Notes and actions MUST live in their own rows. Opening edit MUST start with every section collapsed. `TasksViewModelTest.edit_startsCollapsed_toggleOpensName` locks the start state.

#### Scenario: Edit opens as a menu
- **WHEN** the user opens an existing task
- **THEN** no edit section is expanded

#### Scenario: A row toggles
- **WHEN** the user taps the name row twice
- **THEN** the name section is expanded after the first tap and collapsed after the second
