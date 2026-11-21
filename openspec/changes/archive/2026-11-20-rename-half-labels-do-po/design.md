# Design

## Context

See proposal.md for why. `AmPmRow` and `DualTimeDial.halfLabel` already bind `dial_am`, `dial_pm`, `cd_half_am`, and `cd_half_pm`. Russian visible copy is already ДО and ПО. English copy and both content descriptions still say AM/PM. `DialHalf.AM` is `[00:00, 12:00)` and `DialHalf.PM` is `[12:00, 24:00)`. `SetDialHalf` only replaces `DayUiState.dialHalf`.

## Goals / Non-Goals

**Goals:**

- Replace the four string values so labels and TalkBack match the day-dial delta.
- Keep the enum names and the half-switch path that does not rewrite blocks.
- Lock the copy and the 9:00 minute with unit tests.

**Non-Goals:**

- Renaming `DialHalf`, a side menu, the timer, Room fields, or dial geometry.

## Decisions

1. **Edit the existing resource names.** `AmPmRow`, `ViewModeRow`, and `DualTimeDial` already branch on `DialHalf.AM` versus `DialHalf.PM` and load these four ids. New ids would duplicate the switch. Alternative considered: a Kotlin label table in `kit.core`. The UI already owns localized copy in `values` and `values-ru`, so the table would drift from TalkBack.

2. **Allow the English label to wrap.** “Before noon” is longer than “AM”. `AmPmRow` currently uses one line and ellipsis, which would hide the required words. Set `maxLines` to 2 and allow soft wrap on that text only. Russian ДО and ПО stay one short word.

3. **Test the catalogs as files.** JVM unit tests do not inflate Android resources. A test reads `values/strings.xml` and `values-ru/strings.xml` from the repo and asserts the four names. `DialHalfTest.workBlock_isAmOnly` already locks a 9:00 start on the morning face; add an assertion that selecting the other half does not mutate the stored blocks.

## Risks / Trade-offs

- [English “Before noon” wraps on a narrow face] → two lines on the segmented button, not a shorter abbreviation that brings AM/PM back.
- [A later string edit reintroduces AM/PM] → the catalog test fails on those four names.

## Migration Plan

String-only. No data migration. Rollback is restoring the four strings.

## Open Questions

None.
