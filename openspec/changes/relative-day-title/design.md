# Design

## Context

See proposal.md for why. `DayScreen` passes `DateTimeFormatter.ofPattern("EEE, d MMM")` as `PaperDateHeader.title` and sets `showDate = false`. `PaperDateHeader` is shared with Tasks, which keeps the default title and the medium date line. `DayUiState.date` is the visible day. Local today is `TimeProvider.today()`, already read on each clock tick in `DayViewModel.observeDay`. `kit.core` must not take `java.time`.

## Goals / Non-Goals

**Goals:**

- Classify the visible date against local today as today, yesterday, tomorrow, or other, and map the first three to string resources.
- Keep the short date formatter for every other day.
- Refresh the word when local today changes, including while a chosen date stays selected.

**Non-Goals:**

- Changing `PaperDateHeader` defaults, the Tasks strip, the side panel title, or stored dates.
- Locale-specific relative phrases beyond the Russian and default English catalogs.

## Decisions

1. **Pure classifier in `ui.day`, not in `kit.core`.** A function takes the visible `LocalDate` and local today and returns today / yesterday / tomorrow / other by comparing calendar dates (`minus` / `plus` one `DateTimeUnit.DAY`). The composable maps the three words to `R.string` and leaves other dates on the existing formatter. Alternative: format the string inside the ViewModel. Rejected because copy belongs in resources, and the classifier stays unit-testable without Android.

2. **Carry local today on `DayUiState`.** Add `localToday: LocalDate? = null` and set it from `timeProvider.today()` in the single full `DayUiState(...)` built in `observeDay`. The clock tick already rebuilds that state at midnight, so a pinned yesterday becomes "other" after the day rolls, and a followed today stays "today" because `visible-day` moves `date`. Alternative: read the device clock inside `DayScreen`. Rejected because the screen does not own time, and previews would not be able to pin a today.

3. **Title case in both catalogs.** `day_relative_today` / `day_relative_yesterday` / `day_relative_tomorrow` are Сегодня, Вчера, Завтра and Today, Yesterday, Tomorrow. A string-catalog test locks the six values, same style as `HalfLabelStringsTest`. The classifier test locks the four outcomes, including a date two days away.

## Risks / Trade-offs

- [A state rebuild that forgets `localToday`] → only the `observeDay` constructor sets the field; `copy` sites keep it. A null today falls back to the short date, which matches the loading strip.
- [Weekday text still follows the device locale] → unchanged for other days; the three words are explicit in both catalogs.
- [`PaperDateHeader` still formats a medium date when `showDate` is true] → the day screen keeps `showDate = false`, so that line stays hidden.

## Migration Plan

No stored data and no Room version change. Rollback is restoring the short-date title in `DayScreen`.
