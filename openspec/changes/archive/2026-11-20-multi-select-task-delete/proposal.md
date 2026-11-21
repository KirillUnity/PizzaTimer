# Proposal: Multi-select tasks for delete

## Why
The tasks tab can only delete from the editor, one row at a time. Duplicate leftover rows (and any real pile-up) need a way to mark several tasks and remove them together.

## What Changes
- Long-press a row to start selection; tap toggles while selecting.
- A bar shows the count, cancel, and delete.
- Confirm still asks. Confirm removes each selected task, its intervals, and its break wedges. Other tasks stay.
- Break wedges stay off the list, so they are not selectable.

## Capabilities
- `task-list`: multi-select delete on the tasks tab.

## Impact
`TasksUiState`, `TasksViewModel.confirmDelete`, `TaskListRow`.
