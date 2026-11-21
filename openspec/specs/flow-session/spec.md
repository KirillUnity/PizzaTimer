# flow-session Specification

## Purpose
Preserve the live session: pause keeps elapsed time, a stop note can be resumed, and day close does not rewrite history.

## Requirements

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

### Requirement: Untitled Play is a normal open session
An interval opened by Play without a selection MUST follow the same append-only session rules as a start on an existing task. Already ended intervals MUST stay unchanged. The completion sheet MUST NOT open in this step.

#### Scenario: Other closed intervals stay put
- **WHEN** Play creates a new task while other tasks already have ended intervals
- **THEN** those ended intervals are unchanged and the new task has one open interval

### Requirement: Play asks for a 15-minute length
Before an interval opens, the system MUST ask for a length. The step MUST be 15 minutes. When the task already has an end on the dial and 40 minutes remain, the offer MUST be 30 minutes. When the task has no end, the offer MUST be 25 minutes. The user MUST be able to change the length before the interval opens. The offer MUST be whole minutes. `SessionDurationTest` locks the 40-minute and untimed offers.

#### Scenario: Forty minutes offers thirty
- **WHEN** the planned end is 40 minutes ahead
- **THEN** the offered length is 30 minutes

#### Scenario: No end offers twenty-five
- **WHEN** the task has no end on the dial
- **THEN** the offered length is 25 minutes

### Requirement: The session hole shows minutes left
While a work interval is open, the hole MUST show the task name and one quiet line of minutes left. It MUST NOT show a percent, a red ring, ratings, or the day’s done total. While a break is open and the work task is selected, the hole MUST keep that minutes-left line and MUST use the break caption instead of the this-session caption.

#### Scenario: Open session hides the day total
- **WHEN** a task has an open interval
- **THEN** the day face does not show the done-today line and the hole shows minutes left

### Requirement: Pause shows the break title in the hole
After Pause, the hole MUST show the break title («Перерыв») even when the inner diameter is below the session-detail threshold. Play and Pause MUST stay icon-only. `HoleTimerControlTest.pause_showsBreakCaption` locks the caption.

#### Scenario: Compact hole after Pause
- **WHEN** the user pauses a running work interval
- **THEN** the hole shows the break title and a Play icon

### Requirement: The chime closes the interval once
When the chosen minute arrives, the system MUST close the open interval on that minute and play one short in-app sound. A later tick MUST NOT play the sound again and MUST NOT append another interval. Already ended intervals MUST stay unchanged. The timer MUST NOT change its own length. No notification channel is created. `DayViewModelTest.timedStart_closesOnEndMinuteOnce` locks the close.

#### Scenario: Clock reaches the end minute
- **WHEN** a 15-minute session reaches its end minute
- **THEN** the open interval ends on that minute, older intervals are unchanged, and a second interval is not added

#### Scenario: A later tick does not chime again
- **WHEN** time moves past the end minute after the interval has closed
- **THEN** the sound is not requested again

### Requirement: Pause starts a break
Pause MUST close the open work interval at the current minute and MUST start one break wedge from that minute. Pause MUST NOT open the stop-note slip. Already ended intervals MUST stay unchanged. The break MUST be visible on the dial. `DayViewModelTest.pause_startsBreak_andLeavesClosedBlocks` locks this.

#### Scenario: Pause leaves the old interval and skips the note
- **WHEN** the user pauses a running task that already has an ended interval
- **THEN** that ended interval is unchanged, the open interval is closed, a break starts at the current minute, and the stop-note slip stays closed

### Requirement: Continue and finish the break
While a break is open, the day MUST show one centered filled button to finish the task and MUST NOT show Continue on the face. Continue MUST still close the break and append exactly one new open work interval on the same task when Play is pressed on the paused hole. Finish task MUST close the break, hide that button, and MUST open the completion sheet. A Play on an interval that is already open MUST NOT append another interval. `DayViewModelTest.continue_appendsOneWorkInterval_playDoesNotDouble` locks continue and the second Play.

#### Scenario: Continue appends one work interval
- **WHEN** the user continues after a pause
- **THEN** the break is closed and the work task has one new open interval after the intervals that were already closed

#### Scenario: Finish hides the break actions
- **WHEN** the user finishes the task from the break
- **THEN** the break is closed, the finish and continue actions are not offered, and the completion sheet is open

#### Scenario: Second Play does not append
- **WHEN** the user presses Play while that new work interval is still open
- **THEN** no further interval is appended

### Requirement: Deleting work clears its break
Deleting a work task MUST also delete every break wedge that points at that task. After that delete the day MUST NOT show Finish task. `DayViewModelTest.deleteWorkWhilePaused_removesBreak` locks this.

#### Scenario: Delete during a paused Play
- **WHEN** a break is open and the user confirms delete of the work task
- **THEN** the break row is gone and Finish task is not offered

### Requirement: Completion sheet requires the three proxies
The completion sheet MUST collect state-after, engagement, and skill as integers from 1 to 10. It MUST NOT show a self-report or clinical-scale caption. It MUST NOT offer an “I am in flow” checkbox and MUST NOT use the words FSS-2. Submitting with any of those three missing MUST still write a completion, filling each missing proxy with 5 and an unset mark with challenge, so the user can edit the record later. `DayViewModelTest.completion_emptySave_storesDefaults_andReloadEdits` locks save and reload.

#### Scenario: Empty state is refused
- **WHEN** the user submits the sheet without a state-after value
- **THEN** a completion is stored with proxy 5 for the missing fields and the sheet closes

### Requirement: Short state form is optional
The completion sheet MUST keep state, engagement, and skill required. It MAY collect the 9-item short flow-state form. The caption for that form MUST be a prompt to rate on a 5-point scale. It MUST NOT mention diagnosis or paraphrased questionnaire items. Skipping that form MUST still save the 1–10 state. The sheet MUST NOT diagnose flow and MUST NOT say the user is in flow. `ShortFlowScaleTest.skippedForm_keepsState` locks the skip.

#### Scenario: Skip keeps the proxy
- **WHEN** the user saves a completion with a state proxy and skips the short form
- **THEN** the stored state proxy remains and the form profile is absent

### Requirement: Dispositional form is asked once
The app MUST NOT show the 9-item short dispositional form after the introduction or at any other time. Finishing or skipping the introduction MUST open the day. The introduction pages themselves stay. The optional 9-item short state form on the completion sheet MUST stay available and skippable. `ShortFlowScaleTest.skippedForm_keepsState` still locks that skip.

#### Scenario: A finished form stays finished
- **WHEN** the user has already completed or skipped the dispositional form
- **THEN** onboarding does not ask for it again

#### Scenario: Intro opens the day
- **WHEN** the user finishes or skips the introduction
- **THEN** the next screen is the day face, not a dispositional questionnaire

### Requirement: Saving a completion clears selection
A successful completion save MUST close the sheet, MUST set selected task to none, and MUST close the details sheet. `DayViewModelTest.completion_save_clearsSelection` locks the clear.

#### Scenario: Save leaves the dial idle
- **WHEN** the user submits a valid completion
- **THEN** the completion sheet is closed and no task is selected

### Requirement: Pause keeps the work hole
Pause MUST keep the work task selected. The hole MUST show that task’s name and minutes left when a budget exists. The caption MUST be the break title and MUST NOT be «этот заход». Pause MUST NOT show IN FLOW or elapsed on that hole. `DayViewModelTest.pause_whileBreakOpen_keepsWorkSelected` locks the selection.

#### Scenario: Pause copy is the break title
- **WHEN** the user pauses a running work interval
- **THEN** the selected task is the work task, not the break wedge

#### Scenario: Pause after the work sector is already selected
- **WHEN** a break is open and Pause is pressed again
- **THEN** the work task stays selected and its closed intervals stay unchanged

### Requirement: Hole play and pause swap
The hole control MUST show Pause while a non-break work interval is open and MUST show Play when idle or when a break is open. Pressing Pause MUST pause. Pressing Play MUST start or resume. `HoleTimerControlTest.openWork_isPause_breakOrIdle_isPlay` locks the pairing.

#### Scenario: Start then pause
- **WHEN** the user starts a session from Play
- **THEN** the hole control is Pause

#### Scenario: Pause then play
- **WHEN** the work interval is closed and a break is open
- **THEN** the hole control is Play

### Requirement: Start and pause are icons
The start control and the pause control in the dial hole MUST show an icon and MUST NOT show a text label. The control name MUST remain available to accessibility. After Pause the hole MUST still show the break title as a caption, not as a button label.

#### Scenario: Idle start
- **WHEN** the user can start a sitting from the hole
- **THEN** the control shows a play icon and no word «Старт» on the hole

#### Scenario: Running pause
- **WHEN** a sitting is open
- **THEN** the control shows a pause icon and no word «Пауза» on the hole
