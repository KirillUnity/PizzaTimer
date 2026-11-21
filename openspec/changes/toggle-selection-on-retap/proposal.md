# Proposal: Clear selection on a second tap

## Why
A selected sector stays selected when the user taps it again. Pause also re-applies the work task in the day host if selection is cleared. The callout cannot be dismissed by tapping the same task.

## What Changes
- A second `SelectTask` on the same id still clears selection.
- A second `OpenTaskDetails` on the same open sheet clears selection and closes the sheet.
- A tap that does not hit a task, or a handle grab with no resize, dismisses.
- The day host MUST NOT re-select the paused work after the user clears selection.

## Capabilities
- `day-dial`: retap clears the selected sector.

## Impact
Dial, pizza, petals, and the day list. Pause still selects work when Pause is pressed.
