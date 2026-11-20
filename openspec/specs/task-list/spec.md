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
