# Spec Delta

## ADDED Requirements

### Requirement: Stop note and mood survive the session
The system MUST persist a committed pause note and its mood (Calm, Deep, or Stuck) for that task across a new process. A whitespace-only commit MUST NOT replace a saved note or its mood. `DayViewModelTest.pause_opensSlip_commitKeepsClosedBlocks` locks that a commit does not rewrite closed intervals.

#### Scenario: Commit note and mood
- **WHEN** the user commits a pause slip with a non-blank note and a selected mood
- **THEN** the stored note is the trimmed text and the stored mood is the selected mood

#### Scenario: Blank commit keeps the previous note
- **WHEN** the user commits a whitespace-only slip and a note already exists for that task
- **THEN** the previous note and mood remain

#### Scenario: New session shows the same chip
- **WHEN** a new session loads the same stored data and the user selects that task while it has no open interval
- **THEN** the resume chip shows the stored note and the same mood label as the pause slip

### Requirement: Skip does not change a stored note
Skip MUST close the pause slip and MUST NOT write or delete any stored pause note.

#### Scenario: Skip leaves another task's note
- **WHEN** the user skips the slip on one task while another task already has a stored note
- **THEN** the other task's note and mood are unchanged

### Requirement: Start removes the stored note
Start MUST delete the stored note for that task, hide the chip, and append a new interval. Closed intervals MUST stay unchanged. `DayViewModelTest.startThenPause_appendsSecondBlock` locks append-only intervals.

#### Scenario: Start clears the chip
- **WHEN** the user starts a task that has a stored note
- **THEN** the stored note is gone, the chip is hidden, and a new interval is appended without rewriting closed intervals

### Requirement: Changing day hides the slip without deleting notes
Changing the visible day MUST hide the pause slip and the day-close card and MUST NOT delete stored pause notes.

#### Scenario: Leave and return
- **WHEN** the user changes the visible day after a note was stored
- **THEN** the slip and card are hidden and the stored note is still present on return

### Requirement: Day close is remembered per local date
The system MUST remember an explicit day close for that local date. Entering that date MUST show the card until the user dismisses it. The next local date MUST NOT show the card on its own. Dismissal MUST last until the user closes that same date again.

#### Scenario: Same date shows the card
- **WHEN** the user closed local date D and later enters D before dismissing the card
- **THEN** the day-close card is visible

#### Scenario: Next date hides the card
- **WHEN** the user enters the local date after D
- **THEN** the day-close card is not visible

#### Scenario: Dismissal stays hidden
- **WHEN** the user dismisses the card for D and later enters D again
- **THEN** the card stays hidden until the user closes D again

### Requirement: Day close does not lock the day or create a report
The day-close card MUST NOT block Start, edit, or new tasks, and MUST NOT create a task report. "To reports" MUST open the reports tab and MUST NOT create a task report. When closed minutes are zero, the card MUST show the calm no-closed-intervals line.

#### Scenario: Reports navigation
- **WHEN** the user chooses "To reports" on the day-close card
- **THEN** the reports tab opens and no task report is created

#### Scenario: No closed minutes
- **WHEN** the day-close card is shown and the day has no closed minutes
- **THEN** the card uses the calm no-closed-intervals text
