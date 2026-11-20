# Design

## Context

See proposal.md for why. Pause text, mood, and `showDayClose` are fields on `DayUiState` rebuilt from the current ViewModel. `TaskTimer.pause` already closes the open interval. Appearance and accepted plans already use Preferences DataStore. Room uses `fallbackToDestructiveMigration()`, so a new task column is out.

## Goals / Non-Goals

**Goals:**

- Persist the checkpoint and the day-close flag outside the ViewModel.
- Drive the chip and the card from that store inside the existing day MVI loop.

**Non-Goals:**

- Room schema, Hilt, KMP, a fifth tab, practice, photo background, notifications, locking the day, creating a `TaskReport`.

## Decisions

1. **Preferences DataStore, new file `timedial_flow_memory`.** Checkpoint and day-close are not appearance. A separate store keeps the appearance codec unchanged. Alternative: Room columns — rejected because a schema bump wipes the database. Alternative: the appearance file — rejected to avoid mixing keys.

2. **`FlowMemoryStore` in `kit.core`, implementation in `data`.** The interface exposes observe/save/clear for a task id and mark/dismiss for a `LocalDate`. `PauseMood` moves to `kit.core` so the store does not import `ui`. `kit.render` does not see DataStore. Koin binds the DataStore implementation. Tests use an in-memory implementation shared by two `DayViewModel`s.

3. **Encoding.** One string-set entry per task: escaped task id, mood name, escaped text, separated by a unit separator. Closed dates and dismissed dates are ISO `LocalDate` string sets. A blank commit does not call save. Skip does not call the store. Start calls clear for that task only.

4. **Chip and card are derived.** On each day snapshot, if the selected task has a stored note and no open interval, `resumeCheckpoint` and `resumeMood` come from the store. Otherwise the chip is hidden. `showDayClose` is true when the visible date is in the closed set, not in the dismissed set, and the pause slip is closed. Changing the date clears the slip locally and does not write the store. `ShowDayClose` adds the date and removes its dismissal. `DismissDayClose` adds the dismissal. The existing "To reports" handler still sends `DismissDayClose` and then navigates; it does not touch `ReportRepository`. Start does not dismiss the card, so the card does not block the timer.

5. **Zero closed minutes.** `DayCloseCard` uses a new `flow_closed_none` string (values and values-ru: «Закрытых отрезков нет») when the sum is zero. The chip shows the existing mood strings beside the note.

## Risks / Trade-offs

- [Chip hidden until the sector is selected] → Matches the prompt. The unit test selects the sector on the second ViewModel.
- [Reports button also dismisses the card] → That is the only hide control today. Dismissal is what gets remembered. The button still does not create a report.
- [Corrupt preference entry] → Decode skips that entry and falls back to an empty snapshot on `IOException`, same as appearance.

## Migration Plan

New preferences start empty. No Room version change. Rollback is removing the Koin binding and the ViewModel parameter; tasks and intervals stay in Room.
