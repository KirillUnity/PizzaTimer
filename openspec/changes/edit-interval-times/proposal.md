# Proposal

## Why

A finished interval could not be changed or removed, and the editor showed only the clock range. Each interval needs an expected length and the length it actually took (mission §2.1, the edge of a step).

## What Changes

- Any interval in the editor can change its start and end and can be removed, including one that has already ended.
- Each interval stores expected minutes. Actual minutes are the clock length, or start-to-now while the interval is still open.
- Changing the clock range does not change the expected minutes. Starting a timed session stores that length as the expected minutes.
- Start and Pause still do not rewrite an interval that has already ended.

## Capabilities

### Modified Capabilities

- `task-list`: ended intervals in the editor are editable and removable, and each interval has expected and actual minutes.

## Impact

`TimeBlock.plannedMinutes`, the interval editor, and Room schema v9 (destructive migration, local data is wiped).
