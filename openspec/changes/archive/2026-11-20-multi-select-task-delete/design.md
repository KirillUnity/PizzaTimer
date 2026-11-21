## Context
Rows currently open the editor on tap. Delete lives in the editor sheet.

## Decision
Keep tap-to-edit when nothing is selected. Long-press adds that `TaskId` to `selectedTaskIds`. Further taps toggle membership. Empty selection leaves the mode. `RequestDeleteSelected` sets `pendingBulkDelete`; the existing confirm dialog covers one or many. Confirm walks the selected ids and reuses wedge cleanup from single delete.

Closed `TimeBlock`s are not rewritten; they go away with the task.

## Risks
Tap vs edit: only toggle while `selectedTaskIds` is not empty. Day change clears the set.
