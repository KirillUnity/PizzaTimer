# Proposal

## Why

Play starts a session immediately, with no length and no quiet end. Mission §8.3 asks for a soft countdown that starts work, and principle 7 (section 9) keeps the immersion moment to this session only. This change asks for a length before the interval opens, shows only the minutes left in the hole, and closes that interval once with a single in-app sound. It does not shorten or stretch the timer when “flow signs” appear: that signal does not exist yet.

## What Changes

- Before Play opens an interval, the user sets a length in 15-minute steps. A task that already ends on the dial offers the remainder floored to 15 minutes (40 minutes offers 30). A task with no end offers 25 minutes. The length can change before confirm.
- The running hole shows one quiet “minutes left” line, the task name, and the pause icon. No percent, no red ring, no “done today” line, no ratings.
- When that minute arrives, the open interval closes on that minute and one short sound plays. The sound does not repeat. There is no notification channel, so a dark screen is not promised a chime.
- Closed intervals stay unchanged. A second interval is not appended by the chime.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `flow-session`: A chosen length closes the open interval once. Pause notes, append-only starts, and day close stay as specified.

## Impact

- `SessionDuration` in `kit.core`, `DayViewModel` budget watch, a duration sheet, the hole line, `res/raw/session_chime.wav`.
- Unit tests for the 40-minute offer and for closing on the end minute.
- No Room change, no notification channel, no adaptive shortening.
