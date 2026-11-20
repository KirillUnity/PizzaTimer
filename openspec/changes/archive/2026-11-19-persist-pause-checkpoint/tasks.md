# Tasks

## 1. Flow memory model

- [x] 1.1 Move `PauseMood` to `kit.core` and add `FlowMemoryStore` plus an in-memory implementation and a preference codec. Verify a unit test round-trips a note with a separator in the text and skips a corrupt entry.

## 2. DataStore and day MVI

- [x] 2.1 Add `DataStoreFlowMemoryStore` (`timedial_flow_memory`) and bind it in Koin. Verify the app module resolves `FlowMemoryStore` without a Room version change.
- [x] 2.2 Derive the resume chip and day-close card in `DayViewModel` from the store: trimmed non-blank commit saves text and mood, blank commit and Skip do not write, Start clears that task, a date change does not delete notes, day close and dismiss are per local date. Verify `DayViewModelTest` covers a second ViewModel seeing the chip, Start clearing it, Skip leaving another task's note, and date D visible versus D+1 hidden.

## 3. Chip and empty card copy

- [x] 3.1 Show the stored mood on `ResumeCheckpointChip` with the existing mood strings, and show `flow_closed_none` in values and values-ru when closed minutes are zero. Verify the day-close preview still builds and the Russian string is «Закрытых отрезков нет».

## 4. Suite

- [x] 4.1 Run `gradlew.bat :app:testDebugUnitTest` and verify the new tests and the suite pass before archive.

## Workflow follow-up

- Archive `persist-pause-checkpoint` only after `:app:testDebugUnitTest` passes.
