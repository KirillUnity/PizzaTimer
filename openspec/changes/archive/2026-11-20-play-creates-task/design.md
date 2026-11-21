# Design

## Context

See proposal.md for why. `DayViewModel.startTimer` returns when `selectedTask` is null. `TaskTimer.start` appends an open block and returns the same task when one is already open. The idle hole draws `LiveChronoHole` and has no play control until a sector is selected. The play control is already an icon.

## Goals / Non-Goals

**Goals:**

- Create one untitled task and one open interval from Play with no selection.
- Ignore Play when any task on the day already has an open interval.
- Show the existing play icon in the idle hole.

**Non-Goals:**

- A duration, a sound, a break, a side menu, or a completion sheet. The title stays empty until a later step.

## Decisions

1. **Create in `startTimer`, then call `TaskTimer.start`.** The new task is a normal `Task` with an empty title, the visible date, and the current accent. Alternative considered: a special untimed placeholder. That would not be an open session.

2. **Guard on any open block, not only the selection.** A second Play, or Play after the running task was dismissed, must not fork another open interval. The selected running task still uses `TaskTimer.canStart`.

3. **Select the new task before the write.** The day rebuild keeps the selection whose id is in the repository, so the hole switches to the running session after the upsert.

## Risks / Trade-offs

- [An empty title looks blank in the hole] → the completion sheet, not this step, asks for the name.
- [Sample data may already fill an “empty” debug day] → Play still adds one task when nothing is selected and nothing is open.

## Migration Plan

No data migration. Rollback restores the early return.

## Open Questions

None.
