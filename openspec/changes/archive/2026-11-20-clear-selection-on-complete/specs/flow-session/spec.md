## ADDED Requirements

### Requirement: Saving a completion clears selection
A successful completion save MUST close the sheet, MUST set selected task to none, and MUST close the details sheet. A refused save MUST leave the completion sheet open and MUST keep the current selection. `DayViewModelTest.completion_save_clearsSelection` locks the clear.

#### Scenario: Save leaves the dial idle
- **WHEN** the user submits a valid completion
- **THEN** the completion sheet is closed and no task is selected
