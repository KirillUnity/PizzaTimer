# Proposal: Delete clears the leftover break

## Why
During a Play session the user can delete the work task. The sand break wedge can stay, so «Завершить задачу» never leaves: Finish looks up the missing work and returns.

## What Changes
- Deleting a work task also deletes its break wedges.
- Finish is shown only when an open break still has a work task.
- Finish with a missing work task closes the orphan break.

## Capabilities
- `flow-session`: no Finish button after the work task is gone.
- `task-list`: confirm delete still removes the task and now its break wedge.

## Impact
`DayViewModel.confirmDelete`, `TasksViewModel.confirmDelete`, `DayScreen` Finish visibility.
