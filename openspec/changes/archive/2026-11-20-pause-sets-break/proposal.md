# Proposal

## Why

Pause closes the open interval and opens the “where I stopped” slip. Mission principle 7 (section 9) makes a pause a recovery, not another form. This change turns Pause into a break on the dial. The stop-note slip is no longer the pause action. Ratings stay for the completion sheet.

## What Changes

- Pause closes the open work interval and starts a sand break wedge from the current minute. It does not open the stop-note slip.
- The day shows «Завершить задачу» and «Продолжить» while that break is open.
- Continue closes the break and appends one new open work interval on the same task.
- Finish task closes the break and hides those buttons. No ratings sheet.
- Already ended intervals stay unchanged. Play on an already open interval still does not append another one.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `flow-session`: Pause starts a break. Closed intervals, the chime, and a later note commit stay as specified.

## Impact

- `BreakSession` in `kit.core`, `DayViewModel` pause / continue / finish, two buttons on the day, updated `DayViewModelTest`.
- No Room schema change, no new sound, no 1–10 scales.
