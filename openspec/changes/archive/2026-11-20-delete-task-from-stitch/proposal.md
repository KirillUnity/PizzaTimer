# Proposal

## Why

Delete already asks for confirmation, but the paper frame asks for a specific dialog: keep is the terracotta action, delete is plain text, and the copy says the task and its intervals leave this day. Mission principle 6 (section 9) keeps that data on the device and removes it only after a yes. Frame `_90` is the reference. Frame `_83` is not.

## What Changes

- The day delete dialog uses the title «Удалить это дело?», a body that the task and its intervals disappear from this day, a terracotta «Оставить» button, and a plain-text «Удалить» button with no red fill.
- Confirm is required. «Оставить» deletes nothing.
- Confirm removes the task, its intervals, and that task’s scores. Neighboring tasks stay where they are.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `task-list`: delete confirmation matches the paper dialog and removes the task’s scores with the task.

## Impact

- Day delete dialog and `DayViewModel` delete path. The task-editor dialog is unchanged.
- No Room schema change, no trash, no undo.
