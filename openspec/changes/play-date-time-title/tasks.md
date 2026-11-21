# Tasks

## 1. Stamp

- [ ] 1.1 Add `TimeMath.provisionalTitle(date, startMinute)` as `dd.MM.yyyy HH:mm` using `formatMinuteOfDay`. Extend `TimeMathTest` so 20 November 2026 at minute 17:58 is `20.11.2026 17:58`.

## 2. Play without a selection

- [ ] 2.1 In `DayViewModel.startTimer`, set that stamp on the task created when nothing is selected. Leave a selected task's title unchanged. Update `DayViewModelTest.playWithoutSelection_createsOneOpenInterval_secondPlayDoesNotAppend` for date `2026-10-06` at 09:30 (`06.10.2026 09:30`), one open interval, selection, and no second task. Add a case that starting a selected titled task does not rename it.

## 3. Replace later

- [ ] 3.1 Treat a title as editable on the completion sheet when it is blank or still equals `provisionalTitle` for the task date and earliest block start. Use that check in `CompletionDraft.from` and in the draft `finishBreak` builds. Add a `DayViewModelTest` that saving `Черновик` over `20.11.2026 17:58` stores `Черновик`, and that a user-written title stays non-editable.

## 4. Check

- [ ] 4.1 Run `:app:testDebugUnitTest` and confirm it passes before archive.

## Workflow follow-up

- Archive `play-date-time-title` after the unit tests pass.
