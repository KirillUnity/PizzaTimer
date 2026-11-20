# Spec Delta

## Purpose

Preserve the live session: pause keeps elapsed time, a stop note can be resumed, and day close does not rewrite history.

## ADDED Requirements

### Requirement: Pause keeps elapsed time
The system MUST end only the open interval when the user pauses, and MUST leave already-ended intervals unchanged.

#### Scenario: Pause while a session is open
- **WHEN** the user pauses a running task
- **THEN** the open interval gains an end at the current minute and every older interval stays as stored

### Requirement: Stop note becomes the resume point
The system SHALL let the user write where they stopped. Committing a non-empty note sets the resume checkpoint. Committing an empty note keeps the previous checkpoint. Starting again clears the checkpoint.

#### Scenario: Commit a stop note
- **WHEN** the user commits a pause slip with a non-empty note
- **THEN** that note is the resume checkpoint

#### Scenario: Commit an empty slip
- **WHEN** the user commits a pause slip with no note and a checkpoint already exists
- **THEN** the previous checkpoint remains

#### Scenario: Start clears the checkpoint
- **WHEN** the user starts the task again
- **THEN** the resume checkpoint is cleared

### Requirement: Day close sums ended time only
The system SHALL show the day's closed minutes as a duration. Open intervals are excluded. Closing the summary MUST NOT rewrite stored intervals.

#### Scenario: Open slice is excluded
- **WHEN** the day has both ended intervals and one open interval
- **THEN** the closed-minute total includes only the ended intervals
