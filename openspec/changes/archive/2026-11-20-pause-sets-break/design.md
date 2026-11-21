# Design

## Context

See proposal.md for why. `pauseTimer` sets `pauseSlipOpen` and calls `TaskTimer.pause`. A break has to be a normal `Task` with an open `TimeBlock`: Room gains no columns. The sand wedge is that open block drawn from now.

## Goals / Non-Goals

**Goals:**

- Tag the break task so the day can find it without a new table.
- Keep the stop-note intents for a commit that is not the pause button.
- Show finish and continue only while a break is open.

**Non-Goals:**

- Scales, the next task, stats, or another end-of-timer sound. The chime still only closes the budget; it does not start a break.

## Decisions

1. **A task tagged `break`.** `description` stores the work task id. The color is sand `0xFFEADBCE`. The visible title comes from strings at the screen, so the stored title can stay empty. Alternative considered: a new Room column. The user forbade a schema bump.

2. **Pause returns when nothing is open.** A closed task does not grow a break. The slip flag stays false.

3. **Continue is its own intent.** Play on an already open block still returns. Starting a selected task that is idle still appends one interval, and if a break is open that start closes the break first so two open wedges do not stack.

## Risks / Trade-offs

- [The break is a real task on the day] → finish and continue close it; it is not a second piece of work the user named.
- [An empty stored title] → the screen substitutes «Перерыв» / “Break” before drawing.

## Migration Plan

No data migration. Rollback restores the slip on pause.

## Open Questions

None.
