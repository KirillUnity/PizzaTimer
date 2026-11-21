## ADDED Requirements

### Requirement: New task starts with a chooser
Opening create MUST NOT dump the full editor. The first surface MUST offer creating an untimed task on the selected day in one action, and a configure path that uses skippable stages. `TasksViewModelTest.createTodayQuick_savesUntimedOnSelectedDay` locks the one-tap path.

#### Scenario: One tap puts work on today
- **WHEN** the user chooses create-for-today and the title is empty
- **THEN** an untimed task is saved on the selected day with the default new-task name and the editor closes

### Requirement: Configure path is skippable stages
The configure path MUST show name, when, look, and details as separate stages. Each stage MUST explain why it exists. Skip MUST advance without requiring that stage’s fields. Skip or Next on the last stage MUST save, filling the default name if the title is still blank. `TasksViewModelTest.createWizard_skipAll_savesDefaultName` locks a full skip.

#### Scenario: Skip never blocks save
- **WHEN** the user starts configure and skips every stage
- **THEN** a task is stored with the default name and default look, with no intervals
