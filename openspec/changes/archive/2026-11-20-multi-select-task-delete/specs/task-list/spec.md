## ADDED Requirements

### Requirement: Several tasks can be selected for delete
The tasks tab MUST let the user mark more than one visible task and MUST ask before removing them. Confirm MUST delete each selected task, its intervals, and break wedges that point at it. Tasks that were not selected MUST stay. Break wedges MUST NOT appear as selectable rows. `TasksViewModelTest.confirmDeleteSelected_removesMarked_keepsOthers` locks this.

#### Scenario: Two marked, one neighbor
- **WHEN** the user confirms delete for two selected tasks while a third task is on the same day
- **THEN** the two are gone and the third remains

#### Scenario: Leave keeps the marked set
- **WHEN** the user dismisses the bulk delete dialog
- **THEN** every selected task is still stored
