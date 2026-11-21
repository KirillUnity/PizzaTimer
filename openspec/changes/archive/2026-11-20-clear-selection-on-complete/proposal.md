# Proposal

## Why

After the finish sheet saves, the same task stays selected. The sector and timer chrome stay on a session that already ended.

## What Changes

- A successful completion save MUST clear the day selection and close details.
- A refused save (missing proxies) MUST keep the sheet and the current selection.

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `flow-session`: save clears selection.

## Impact

- `DayViewModel.submitCompletion`, `DayViewModelTest`.
