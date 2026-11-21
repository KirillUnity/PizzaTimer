# Proposal

## Why

Save on the finish sheet does nothing unless every 1–10 proxy and a demand mark are set. The user taps Save from the 5-point list and the sheet stays. They want a stored record they can open and edit later.

## What Changes

- Save always writes a completion. Unset 1–10 proxies become 5; an unset mark becomes challenge.
- The same report id is upserted so a later save overwrites.
- Opening scores from the task sheet (or Finish again) reloads the stored draft.

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `flow-session`: Save no longer refuses empty proxies; reopen edits the same record.

## Impact

- `DayViewModel.submitCompletion` / `openCompletion`, `CompletionDraft`, `TaskBottomSheet`, `DayViewModelTest`.
