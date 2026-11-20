# Design

## Context

See proposal.md for why. `DayViewModel.selectedDate` starts at `TimeProvider.today()` and then changes only in `shiftDate` and `SelectDate`. `observeTime()` emits `LocalTime` only; `today()` is read separately. `TaskTimer.pause` closes an open block only when the user pauses. It does not run when the date changes. `seedIfEmpty` runs when the day observation starts, not when the date changes. `RecurrenceMaterializer.ensureVisibleDay` already runs for the visible date.

## Goals / Non-Goals

**Goals:**

- Track whether the visible date is following local today.
- On a clock tick, if it is following and `today()` differs, set the visible date to `today()` and seed the AM/PM half from the new time.
- Keep arrow and calendar selection. Landing on `today()` starts following again.

**Non-Goals:**

- Notifications, widgets, Room migrations, a wider recurrence horizon, auto-pause, editing an open block, the tasks-screen date.

## Decisions

1. **Follow flag, not “date equals today” at tick time.** A user who opened tomorrow before midnight is still on that date after midnight, when it has become today. Comparing equality alone would start following a date they did not ask to follow. The flag starts true. Arrows and the calendar set it true only when the chosen date equals `today()` at that moment.
2. **Apply the advance inside the existing time combine**, using `timeProvider.today()` on each tick. `LocalTime` alone cannot tell 23:59 from the next day. Alternative: a second collector. Rejected because the date used to load tasks must change in the same emission as the new time.
3. **Reset the one-shot half seed when the date follows the clock**, so 00:00 shows AM. Manual half changes on the same day stay.
4. **Do not call `TaskTimer` or `seedIfEmpty` on the advance.** Open blocks stay as stored. The new date loads through the existing task observation and `ensureVisibleDay`.

## Risks / Trade-offs

- [Clock jumps several days while the screen follows today] → set the date to `today()`, not to yesterday plus one.
- [Open timer still belongs to yesterday] → left as stored; minutes after midnight are not written into it. Say so in the change output.
- [Pause slip was open at midnight] → the same clear used by arrow navigation hides it, because the visible day changed.

## Migration Plan

No schema or stored-preference change. Rollback is reverting `DayViewModel`.

## Open Questions

None.
