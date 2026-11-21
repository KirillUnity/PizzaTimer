# day-dial Specification

## Purpose
Preserve the clock-face day: geometry, leftover shares, hide-completed reflow, and rotation that does not edit stored times.

## Requirements

### Requirement: Clock geometry
The system SHALL treat 12 o'clock as angle 0, proceed clockwise, and map a full local day of 1440 minutes to a full turn.

#### Scenario: Full day is one turn
- **WHEN** a full local day is mapped onto the dial
- **THEN** 1440 minutes cover 360 degrees starting at 12 o'clock and moving clockwise

### Requirement: Hide completed reflows the dial
On the clock, pizza, and petal views, hiding completed work MUST reflow the remaining work across the face. The list view MUST only filter completed work out. Timed tasks keep their civil hours in wall-clock mode.

#### Scenario: Canvas reflow
- **WHEN** the user hides completed work on a clock-face view
- **THEN** the remaining work is laid out across the face rather than leaving the completed gaps empty

#### Scenario: List filters only
- **WHEN** the user hides completed work in the list
- **THEN** completed rows are omitted and the remaining rows keep their times

### Requirement: Rotation does not edit task times
The system MUST rotate the scene by the user offset plus the automatic now-rotation. Stored task times stay unchanged. With no user offset, now stays at 12 o'clock. Only an interval that has not ended may be resized.

#### Scenario: Now stays at 12
- **WHEN** the user has not applied a rotation offset
- **THEN** the current minute is drawn at 12 o'clock and stored task times are unchanged

#### Scenario: Closed interval cannot be resized
- **WHEN** the user tries to resize an interval that has already ended
- **THEN** that interval stays as stored

### Requirement: Half labels say before noon and after noon
The day face SHALL label 00:00–12:00 as AM and 12:00–24:00 as PM in both the Russian and default English catalogs. Visible labels MUST be the abbreviations AM and PM. Russian TalkBack MUST still read «до полудня» and «после полудня». English content descriptions MUST read “Before noon” and “After noon”. On a clock-face view, the day-look menu button MUST sit at the leading edge of the day header row and the AM/PM control MUST sit at the trailing edge of that same row. The list view MAY omit the half switch. Switching the half MUST NOT change stored task minutes.

#### Scenario: Russian labels and descriptions
- **WHEN** the Russian string catalog is shown
- **THEN** the half controls read AM and PM and their content descriptions are «до полудня» and «после полудня»

#### Scenario: English labels avoid AM and PM
- **WHEN** the default English string catalog is read
- **THEN** the half labels are AM and PM and the content descriptions are “Before noon” and “After noon”
- **AND** `HalfLabelStringsTest.halfLabels_useAmPmAbbreviations` locks the four string names

#### Scenario: Header places the menu then halves
- **WHEN** the day is shown in a clock-face view
- **THEN** the day-look menu is at the leading edge of the header row and AM and PM are at the trailing edge
- **AND** `HalfLabelStringsTest.headerPlacesMenuThenHalves` locks that order

### Requirement: Switching the half does not move stored minutes
Selecting the other half MUST NOT change stored task minutes. A sector placed at 9:00 stays on minute 540. Clock geometry stays 0 degrees at 12 o’clock, clockwise, with 1440 minutes per full turn.

#### Scenario: Nine o’clock stays put
- **WHEN** a sector starts at 9:00 and the afternoon face is selected
- **THEN** the stored start minute is still 540 and the morning clip still begins at 540
- **AND** `DialHalfTest.workBlock_isAmOnly` locks the 9:00 morning slice

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

### Requirement: Sector titles follow the ring
On the clock-face ring, a task title MUST be drawn along the sector’s mid-radius arc, not as a straight chord. Glyphs MUST NOT stack at a single point. Glyphs on the lower half MUST stay upright. Thin slices below `minSweepForLabelDeg` stay unlabeled. `ArcLabelTest.labelArc_reversesOnLowerHalf` and `ArcLabelTest.prepareArcLabels_centersOnArc` lock this.

#### Scenario: Title sits on the arc
- **WHEN** a leftover or timed sector is wide enough for a label
- **THEN** the title is laid out on a circular path at mid-radius of that sector
- **AND** letters follow the arc in reading order instead of overlapping

### Requirement: Pizza hub control is centered
On pizza and petals, the Start (or Pause) control MUST sit at the geometric center of the figure. LIVE CHRONO MAY sit above that control. It MUST NOT shift the control off center. `OverlayHoleFractionTest.pizzaHub_isSmallerThanDialHole` locks the hub size used for those modes.

#### Scenario: Idle pizza centers Start
- **WHEN** the day is in pizza view with no running session
- **THEN** the Start control is at the center of the pie and the live-chrono labels sit above it

### Requirement: A second tap clears the selected task
Tapping the already selected task MUST clear selection and MUST close the details sheet. Tapping leftover rest or a ring miss MUST also clear selection. The day host MUST NOT re-select a paused work task after that clear. `DayViewModelTest.selectTask_secondTapDismisses` and `DayViewModelTest.pause_tapSelectedWork_clearsSelection` lock this.

#### Scenario: Same sector twice
- **WHEN** the user taps a sector that is already selected
- **THEN** no task is selected and the callout is gone

#### Scenario: Pause does not pin selection
- **WHEN** a break is open, the work task is selected, and the user taps that work task
- **THEN** selection is cleared and stays cleared
