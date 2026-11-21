# Proposal

## Why

Pause currently jumps to a break face (IN FLOW, «Перерыв», «этот заход», Finish/Continue). The user wants the second screenshot: the work task stays in the hole (minutes left + title), and on pause the last line is «Перерыв» instead of «этот заход».

## What Changes

- Pause keeps the **work** task selected. It still writes a break wedge on the dial.
- The hole on pause looks like the running session, but the caption is «Перерыв», not «этот заход». No IN FLOW / elapsed.
- Finish/Continue are not shown above the dial. Play on the paused hole resumes (Continue).
- Pause while already paused keeps the work task selected.

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `flow-session`: pause hole copy and selection; break actions off the face.

## Impact

- `pauseTimer`, `RunningSessionHole`, `DialHoleContent`, `DayScreen`, `DayViewModelTest`.
