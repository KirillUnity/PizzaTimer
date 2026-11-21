# Proposal

## Why

Creating a task opens one long sheet (name, color, importance, project, tags, status, intervals, recurrence). Mission principle 1: a low threshold to put work on the day. The user asked for stages with a short explanation and skip, plus a one-tap “create for today” and a path to set parameters.

## What Changes

- New-task FAB opens a chooser, not the full editor.
- **Create for today** saves an untimed task on the selected day immediately (default name if the title is blank).
- **Configure parameters** walks name → when → look → details. Each stage has a one-line why-text, Skip, and Next. Skip on the last stage saves. Back returns to the previous stage.
- Editing an existing task still uses the full sheet. Empty title on a typed Save of the full sheet is still refused. Closed intervals stay immutable.

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `task-list`: new-task create is a chooser plus optional skippable stages.

## Impact

- `TaskEditorState.createStage`, `TasksUiIntent` create-flow intents, `TasksViewModel`, `TaskEditorSheet`, strings, `TasksViewModelTest`.
