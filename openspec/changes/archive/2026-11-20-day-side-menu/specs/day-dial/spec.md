# Spec Delta

## ADDED Requirements

### Requirement: Day look opens from a side panel
The day face MUST NOT show view-mode chips or the hide-completed control. Those controls MUST live in a side panel titled Day (Russian: День). The panel MUST offer dial, pizza, petals, and list, plus accent, completed color, underlay, and hide completed. The day header title stays Day / День.

#### Scenario: Resting day has no view chips
- **WHEN** the day is at rest
- **THEN** the face does not show view-mode chips or hide-completed above the clock

#### Scenario: Panel edits the shared look
- **WHEN** the user changes view or underlay in the panel
- **THEN** the face updates immediately and Settings shows the same view and underlay after a restart

### Requirement: Hide completed keeps reflow and list filter
Hiding completed work from the panel MUST reflow remaining work on the dial, pizza, and petal views. The list MUST only omit completed rows. Importance weights stay 5:3:2. Stored times stay unchanged.

#### Scenario: Canvas reflow still runs
- **WHEN** hide completed is on for a clock-face view
- **THEN** remaining work is laid out across the face
- **AND** `ReflowTest` still locks compact reflow

#### Scenario: List still filters only
- **WHEN** hide completed is on in the list
- **THEN** completed rows are omitted and the other rows keep their times
- **AND** `VisibleListTasksTest.hideCompleted_filtersDoneWithoutChangingOrderOfRest` still locks that filter

### Requirement: A running session keeps the task and this session
While a session is open, the dial hole MUST keep the task name and this session’s time. The view-mode row MUST NOT appear on the face.

#### Scenario: Open session hides the view row
- **WHEN** a task has an open interval
- **THEN** the hole shows that task’s name and the time of this session, and the face has no view-mode row
