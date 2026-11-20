# Proposal

## Why

A running session is the app's current immersion log (mission §6 step «в», principle 7). Pause, the stop note, and day close already work. This change freezes that behavior before later flow work.

## What Changes

- Add a preservation spec for the live session. No Kotlin change.

## Capabilities

### New Capabilities

- `flow-session`: Starting, pausing, noting a stop, and closing the day without rewriting ended intervals.

### Modified Capabilities

## Impact

- Locked by `DayViewModelTest`, `DayFlowHoursTest`, and `TaskTimerTest`.
- The task list and the dial stay as they are. A validated immersion scale (mission §8.1) is not part of this baseline.
