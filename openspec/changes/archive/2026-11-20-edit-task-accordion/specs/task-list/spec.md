## ADDED Requirements

### Requirement: Edit uses expandable sections
Editing an existing task MUST NOT dump every field at once. The sheet MUST list name, when, look, and details as collapsed rows. Tapping a row MUST expand or collapse that section. Notes and actions MUST live in their own rows. Opening edit MUST start with every section collapsed. `TasksViewModelTest.edit_startsCollapsed_toggleOpensName` locks the start state.

#### Scenario: Edit opens as a menu
- **WHEN** the user opens an existing task
- **THEN** no edit section is expanded

#### Scenario: A row toggles
- **WHEN** the user taps the name row twice
- **THEN** the name section is expanded after the first tap and collapsed after the second
