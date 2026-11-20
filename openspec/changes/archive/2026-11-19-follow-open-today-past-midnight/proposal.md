# Proposal

## Why

The day screen sets its date once from the local clock and then only moves it with the arrows or the calendar (mission §9: the current day should stay visible without extra steps). A session left on today therefore stays on yesterday after midnight. Serves mission §9 (awareness of the day the person is in) and §10 (tracking the current day).

## What Changes

- While the visible date is local today, a local date change advances the day screen to the new today: that day's tasks, recurrence for that visible date only, and the AM/PM half from the new time.
- A date chosen with the arrows or the calendar stays put across midnight. Choosing today again follows the clock.
- Do not re-seed the sample day on that advance.
- Do not close or extend an open timer in this change. `TaskTimer.pause` does not run at midnight by itself.

## Capabilities

### New Capabilities

- `visible-day`: Which local date the day screen shows, and when midnight may replace it.

### Modified Capabilities

## Impact

- `DayViewModel` and `DayViewModelTest`.
- Unchanged: dial geometry, hide-completed reflow, rotation, closed `TimeBlock`s, `RecurrenceMaterializer` horizon, Room schema, notifications, the tasks screen date.
