# Proposal

## Why

Closing a task scores the whole task once (mission §2.1 step 6). A day with several intervals of the same task needs a separate close, score, goal, and note on each interval.

## What Changes

- The completion sheet belongs to one interval. Saving it does not mark the task done.
- Each interval keeps its own scores. Opening another interval loads that interval’s scores.
- Each interval can link its own intermediate goal and store its own notes.
- A task with no intervals still uses the task-level goal link.

## Capabilities

### New Capabilities

### Modified Capabilities

- `task-list`: a completion is stored per interval, not as the task’s single record, and it does not change task status.
- `flow-session`: Finish opens the sheet for the interval that just ended.
- `goals`: an intermediate goal may be linked to an interval.

## Impact

Completion report ids, the day sheet, the task editor’s goal and note sections, and a `blockId` column on notes. Room version bumps and wipes local data. Closed interval minutes stay unchanged.
