# Tasks

## 1. Tried flags and week minutes

- [x] 1.1 Add `PracticeSkill`, a set-only `PracticeProgressStore`, and `WeekClosedMinutes.columns` in `kit.core`, plus in-memory and DataStore implementations. Verify `PracticeProgressTest` shows three flags as `3 из 5`, a closed 65-minute block on its weekday column, and an open block adding nothing.
- [x] 1.2 Register the DataStore in Koin. Verify `AppModule` binds `PracticeProgressStore` and no Room version changes.

## 2. Real actions set flags

- [x] 2.1 Mark dial-sector, successful Start, non-empty pause commit, and day close from `DayViewModel`, and an untimed editor save from `TasksViewModel`. Verify a unit test: dial select sets the dial flag, pizza select does not, a blank commit does not set the checkpoint flag, and day close sets its flag. Existing `DayViewModelTest` pause and day-close cases still pass.

## 3. Practice screens

- [x] 3.1 Add the Settings row and the hub, five lessons, and week strip inside Settings, with `@Preview` for 0 of 5 and 3 of 5. Verify the primary lesson action and the empty-hub action call the Day callback, the introduction link calls the existing onboarding callback, and `AppDestinations` still has four entries.
- [x] 3.2 Put user-visible copy in `values` and `values-ru`, drawn with `ui/theme` tokens. Verify no Stitch HTML is added under `:app`.

## 4. Integration check

- [x] 4.1 Run `gradlew.bat :app:testDebugUnitTest` and verify the task exits 0 before archive.

## Workflow follow-up

- Archive `add-instrument-practice` after the unit tests pass, syncing the delta specs.
