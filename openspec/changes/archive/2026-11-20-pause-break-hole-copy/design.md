# Design

## Context

See proposal.md. Pause currently selects the break task, which drives the first-screenshot hub.

## Goals / Non-Goals

- Goal: after Pause, the hub matches the work session (minutes + title) with caption «Перерыв».
- Non-goal: removing the sand break wedge from the ring.

## Decisions

1. **Select work after Pause.** The hole reads the work task.
2. **Caption only.** `paused` swaps `dial_this_session` for `break_title`. Keep minutes left (budget stays).
3. **No face actions.** Play on the paused hole calls Continue.

## Risks / Trade-offs

- [Finish is not on the face] → The completion sheet still opens from FinishBreak; tests call the intent.

## Migration Plan

None.

## Open Questions

None.
