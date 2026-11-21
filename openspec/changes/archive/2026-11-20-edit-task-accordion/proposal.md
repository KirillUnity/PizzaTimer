# Proposal

## Why

Editing an existing task still dumps every field on one sheet. Create already walks name, when, look, and details. The user asked for the same grouping on edit, as expandable menu rows.

## What Changes

- The existing-task editor shows collapsed sections (name, when, look, details, plus goal, notes, and actions when they apply).
- Tapping a row expands or collapses that section. Several sections may be open.
- Save stays at the bottom. Create stays a chooser plus skippable stages.

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `task-list`: edit uses expandable sections, not one long form.

## Impact

- `TaskEditorState`, `TasksUiIntent`, `TasksViewModel`, `TaskEditorSheet`, strings, `TasksViewModelTest`.
