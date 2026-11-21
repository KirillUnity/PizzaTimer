# Design

## Context
`SelectTask` already toggles in `DayViewModel`. The day host then copies `selectedTask ?: workFromBreak`, so a cleared selection comes back during a break. A tap on leftover rest never hits a task. A tap on the resize handle starts resize and cancels without forwarding the tap.

## Goals / Non-Goals
- Goal: a second tap on the selected task, or a tap on leftover, clears selection.
- Non-goals: changing pause-on-press selection, leftover math.

## Decisions
1. Drop `?: workFromBreak` in the day host. Pause still writes the work task in the ViewModel.
2. Missed ring taps call `onDismissSelection`.
3. A resize that does not change `endMinute` is treated as a tap.
4. `OpenTaskDetails` on the already open sheet for that task clears selection.

## Risks / Trade-offs
- [Handle tap deselects instead of grabbing] → Drag still resizes; a still press is a tap.

## Migration Plan
None.

## Open Questions
None.
