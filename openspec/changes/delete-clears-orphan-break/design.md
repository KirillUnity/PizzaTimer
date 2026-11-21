# Design

## Context
A break is a separate Room row tagged `break` with the work id in `description`. `confirmDelete` only removes the selected id. `FinishTaskButton` keys off `BreakSession.openBreak`. `finishBreak` no-ops when the work row is gone.

## Goals / Non-Goals
- Goal: delete work (running or paused) leaves no open break and hides Finish.
- Non-goals: changing how Pause creates a wedge.

## Decisions
1. **`BreakSession.wedgesFor(workId, tasks)`** lists wedges that point at that work.
2. **Day and Tasks delete** remove those wedges with the work task.
3. **Finish is drawn only when `workFromBreak != null`.** `finishBreak` deletes the wedge if work is missing.

## Risks / Trade-offs
None.

## Migration Plan
None. The next delete or Finish tap clears an existing orphan.

## Open Questions
None.
