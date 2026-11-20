# Tasks

## 1. Follow local today

- [x] 1.1 In `DayViewModel`, follow local today across a date change and keep a date chosen with the arrows or the calendar. Verify with `DayViewModelTest.midnight_followsOpenToday`: today at 23:59 becomes the next `LocalDate`, yesterday stays, and returning to today follows the next change. The new half matches the new time. An open block is not closed or extended. The sample day is not seeded by the advance.
- [x] 1.2 Run `gradlew.bat :app:testDebugUnitTest` and verify the suite passes before archive.

## Workflow follow-up

- Archive with the openspec-archive-change skill after the unit tests pass.
