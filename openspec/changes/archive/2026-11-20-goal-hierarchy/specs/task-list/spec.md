# Spec Delta

## ADDED Requirements

### Requirement: A task may link to an intermediate goal
A task MUST save when it has no intermediate goal. When it has one, the next task created from the completion sheet MUST inherit that same intermediate goal. `GoalBookTest.unlinkedTask_andInheritedNext` locks the link. `DayViewModelTest.completion_nextTaskInheritsIntermediate` locks the sheet.

#### Scenario: No intermediate goal
- **WHEN** a task is saved with no intermediate goal
- **THEN** the task is stored and the link map has no entry for it

#### Scenario: Next task inherits
- **WHEN** the completion sheet creates a next task and the current task is linked to an intermediate goal
- **THEN** the next task uses that same intermediate goal
