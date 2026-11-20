# instrument-practice Specification

## Purpose
Record which of the five live-day crafts the person has tried, and show this week's closed minutes, without turning the dial into a course.

## Requirements

### Requirement: Practice opens from Settings
The system MUST open Practice from Settings. While Practice is open, Settings MUST stay the selected top-level destination. The system MUST NOT add a fifth top-level destination.

#### Scenario: Settings stays selected
- **WHEN** the user opens Practice from Settings
- **THEN** the visible destination remains Settings and the other destinations stay Day, Tasks, and Reports

### Requirement: Hub shows five skills and a week strip
The hub MUST show the title Practice, the line «tried · N of 5», a thin progress track, and five skill rows. At zero tried skills the same five rows MUST read not-yet. The hub MUST include seven weekday columns, Monday through Sunday, for the local week that contains today. An empty day MUST stay in that row. The introduction link MUST open the existing onboarding.

#### Scenario: Empty hub
- **WHEN** no skill has been tried and the week has no closed minutes
- **THEN** every skill row reads not-yet and the week says there are no closed intervals

#### Scenario: Three skills tried
- **WHEN** three of the five skills are marked tried
- **THEN** the progress line reads 3 of 5

`PracticeProgressTest.threeFlags_readAsThreeOfFive_andRepeatStaysTried` locks the count fragment.

### Requirement: Skills become tried only on the real action
The system MUST set a durable flag, and MUST NOT clear it, for each of these actions: selecting a sector while the day is in dial mode; saving a task that has no time blocks; a start that succeeds; committing a pause checkpoint whose text is non-empty after trim; closing the day from the day-close card. Repeating the action MUST leave the skill tried. Skip, an empty commit, a failed start, and selecting a task outside dial mode MUST NOT set the matching flag.

#### Scenario: Non-empty checkpoint
- **WHEN** the user commits a pause note that is non-empty after trim
- **THEN** the flow-checkpoint skill is tried and stays tried after the process starts again

#### Scenario: Empty commit does not mark the skill
- **WHEN** the user commits a pause note that is empty after trim
- **THEN** the flow-checkpoint skill stays untried

`DayViewModelTest.commit_survivesNewViewModel_startClearsIt_skipLeavesOther` and `DayViewModelTest.dayClose_isVisibleOnThatDate_andHiddenOnTheNext` still lock checkpoint and day-close memory. This requirement does not clear those records. `DayViewModelTest.practice_dialSelectStartCommitAndDayClose_setFlags_pizzaAndBlankDoNot` and `TasksViewModelTest.update_untimedSaveMarksLeftover_timedSaveDoesNot` lock the flags.

### Requirement: Lessons explain and return to the day
Each skill MUST have a lesson with the short canon text, a static diagram, and a primary action that opens the Day destination. The leftover lesson MUST explain the 5:3:2 shares in words and three labeled parts. It MUST NOT compute dial angles. No lesson MUST mount a second live dial.

#### Scenario: Leftover lesson
- **WHEN** the user opens the leftover lesson
- **THEN** the screen shows three labeled shares and does not run leftover layout

`LeftoverLayoutTest.threeDistinctImportances_are50_30_20` remains the lock for live shares.

### Requirement: Week columns count closed minutes only
The seven columns MUST sum closed minutes of stored blocks on each local date of the week that contains today, including today. An open timer MUST be excluded. A day with no closed minutes MUST remain a column with a zero total and MUST NOT be drawn as an error.

#### Scenario: Closed block lands on its day
- **WHEN** a stored block of 65 minutes is closed on one date of the current week and another block on that date is still open
- **THEN** that date's column is 65 minutes and the open block adds nothing

`PracticeProgressTest.closedSixtyFiveMinutes_landOnThatDay_openBlockDoesNot` locks this scenario.
