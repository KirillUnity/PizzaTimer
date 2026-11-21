# Proposal

## Why

The day face currently labels the two 12-hour rings as ДО and ПО. The user asked to restore the short AM/PM pair and to put that pair on the left of the header, with the day-look menu on the right. Mission principle 1 (section 9) is a low threshold to start the day: a compact Latin pair is faster to scan than ДО/ПО or “Before noon”.

## What Changes

- Visible half labels become **AM** (00:00–12:00) and **PM** (12:00–24:00) in both the Russian and default English catalogs.
- On a clock-face view, AM/PM sit at the **leading** edge of the header row (left in LTR). The day-look menu button sits at the **trailing** edge (right in LTR).
- Half chips leave `DayChromeRow`. Rotation reset stays in that row.
- `DialHalf.AM` / `DialHalf.PM` keep their names. Switching the half still does not move stored minutes.

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `day-dial`: half labels are AM/PM; header places halves on the leading edge and the day-look menu on the trailing edge.

## Impact

- `values/strings.xml` and `values-ru/strings.xml` (`dial_am`, `dial_pm`).
- `DayScreen` header row, `AmPmRow`, `DayChromeRow`.
- `HalfLabelStringsTest`.
- TalkBack may keep the long spoken names (`cd_half_am` / `cd_half_pm`) so the abbreviations stay readable.
