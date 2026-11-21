## ADDED Requirements

### Requirement: A second tap clears the selected task
Tapping the already selected task MUST clear selection and MUST close the details sheet. Tapping leftover rest or a ring miss MUST also clear selection. The day host MUST NOT re-select a paused work task after that clear. `DayViewModelTest.selectTask_secondTapDismisses` and `DayViewModelTest.pause_tapSelectedWork_clearsSelection` lock this.

#### Scenario: Same sector twice
- **WHEN** the user taps a sector that is already selected
- **THEN** no task is selected and the callout is gone

#### Scenario: Pause does not pin selection
- **WHEN** a break is open, the work task is selected, and the user taps that work task
- **THEN** selection is cleared and stays cleared
