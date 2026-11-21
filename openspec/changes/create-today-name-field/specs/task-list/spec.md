## MODIFIED Requirements

### Requirement: New task starts with a chooser
Opening create MUST NOT dump the full editor. The first surface MUST include a title field, creating an untimed task on the selected day in one action, and a configure path that uses skippable stages. `TasksViewModelTest.createTodayQuick_savesUntimedOnSelectedDay` and `TasksViewModelTest.createTodayQuick_usesTypedTitle` lock the one-tap path.

#### Scenario: One tap puts work on today
- **WHEN** the user chooses create-for-today and the title is empty
- **THEN** an untimed task is saved on the selected day with the default new-task name and the editor closes

#### Scenario: Typed name is kept
- **WHEN** the user types a title on the chooser and chooses create-for-today
- **THEN** the stored task uses that title
