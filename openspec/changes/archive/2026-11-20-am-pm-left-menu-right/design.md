# Design

## Context

See proposal.md for why. `DayScreen` currently draws a trailing-only `Row` with the day-look `IconButton`, then `DayChromeRow` draws half chips plus rotation reset. `AmPmRow` already binds `dial_am` / `dial_pm` but is unused on the day face. `DialHalf.AM` is `[00:00, 12:00)` and `DialHalf.PM` is `[12:00, 24:00)`.

## Goals / Non-Goals

- Goals: short AM/PM copy; one header row with halves leading and menu trailing; keep hide-completed and view modes in the side panel.
- Non-goals: renaming `DialHalf`, Room, timer, reflow, Stitch HTML, archiving `icon-only-timer-buttons`.

## Decisions

1. **Reuse `AmPmRow` in the header.** It is already a two-item segmented control. Putting the same chips in `DayChromeRow` would keep them in the flow under the date. Alternative considered: keep `ChromeChip` halves and only flip the menu `Row` to `SpaceBetween`. The unused `AmPmRow` is the tighter control for two short labels.

2. **Keep spoken names long.** Visible AM/PM is what the user asked for. TalkBack still uses `cd_half_am` / `cd_half_pm` so a screen reader does not only hear two letters.

3. **Do not show halves in LIST.** `DayChromeRow` already hid them when `showHalves` is false. The header follows the same `canvasMode` flag.

## Risks / Trade-offs

- [AM/PM is Latin on a Russian face] → Mitigation: TalkBack stays «до полудня» / «после полудня».
- [Two sources of half chips] → Mitigation: `DayChromeRow` stops drawing halves (`showHalves = false` from `DayScreen`).

## Migration Plan

String catalog edit only. No Room version bump. Rollback is reverting the four UI files and the test.

## Open Questions

None.
