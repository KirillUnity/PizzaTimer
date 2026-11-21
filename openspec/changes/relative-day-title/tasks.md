# Tasks

## 1. Relative day classifier

- [x] 1.1 Add a pure `ui.day` classifier that maps a visible `LocalDate` against local today to today, yesterday, tomorrow, or other, and verify a unit test covers all four outcomes including a date two days away
- [x] 1.2 Add `day_relative_today`, `day_relative_yesterday`, and `day_relative_tomorrow` to the default and Russian catalogs (Today / Yesterday / Tomorrow and Сегодня / Вчера / Завтра) and verify a catalog test locks those six values

## 2. Day strip title

- [x] 2.1 Add `localToday` to `DayUiState` and set it from `TimeProvider.today()` in the `observeDay` state build, and verify a pinned date still receives the new today after a clock tick in `DayViewModelTest`
- [x] 2.2 Use the classifier in `DayScreen` for the date-strip title, keep `EEE, d MMM` for other days and the loading string when the date is null, leave `PaperDateHeader` and the Tasks strip unchanged, and verify the day screen source still passes `showDate = false`

## 3. Integration

- [x] 3.1 Run `:app:testDebugUnitTest` and verify it passes before archive

## Workflow follow-up

- Archive `relative-day-title` after the unit tests pass.
