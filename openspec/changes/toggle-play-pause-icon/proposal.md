# Proposal: Play and pause swap on the hole

## Why
The hub always looks like Play, or after pause it still draws pause bars. The control should be Play when idle or on a break, and Pause while a work interval is open.

## What Changes
- Open work → pause icon, click pauses.
- Idle or break → play icon, click starts or resumes.
- Compact hide-completed still shows that control.

## Capabilities
- `flow-session`: hole play/pause icon follows the open work interval.

## Impact
DIAL `RunningSessionHole`, pizza/petals `TimerHoleOverlay`. Finish-task stays on a break.
