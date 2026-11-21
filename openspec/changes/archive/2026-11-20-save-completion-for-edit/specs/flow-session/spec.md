## MODIFIED Requirements

### Requirement: Completion sheet requires the three proxies
The completion sheet MUST collect state-after, engagement, and skill as integers from 1 to 10. It MUST NOT show a self-report or clinical-scale caption. It MUST NOT offer an “I am in flow” checkbox and MUST NOT use the words FSS-2. Submitting with any of those three missing MUST still write a completion, filling each missing proxy with 5 and an unset mark with challenge, so the user can edit the record later. `DayViewModelTest.completion_emptySave_storesDefaults_andReloadEdits` locks save and reload.

#### Scenario: Empty state is refused
- **WHEN** the user submits the sheet without a state-after value
- **THEN** a completion is stored with proxy 5 for the missing fields and the sheet closes

### Requirement: Saving a completion clears selection
A successful completion save MUST close the sheet, MUST set selected task to none, and MUST close the details sheet. `DayViewModelTest.completion_save_clearsSelection` locks the clear.

#### Scenario: Save leaves the dial idle
- **WHEN** the user submits a valid completion
- **THEN** the completion sheet is closed and no task is selected
