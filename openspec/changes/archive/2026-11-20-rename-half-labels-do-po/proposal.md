# Proposal

## Why

The day face still names its two halves AM and PM in English, and TalkBack still says AM/PM in both locales. Mission principle 1 (section 9) is a low threshold to start the day. A Russian reader should see ДО and ПО, and a screen reader should say «до полудня» and «после полудня», without decoding Latin abbreviations. This change only renames the labels. It does not move stored work.

## What Changes

- Visible half labels: Russian ДО (00:00–12:00) and ПО (12:00–24:00); English “Before noon” and “After noon”.
- TalkBack: “до полудня” / “после полудня” in Russian, and the same English phrases without the letters AM or PM.
- `DialHalf.AM` and `DialHalf.PM` stay. Switching the face does not rewrite task minutes. A sector placed at 9:00 stays on those minutes.
- Geometry stays 0° at 12 o’clock, clockwise, 1440 minutes = 360°.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `day-dial`: Half-face labels and content descriptions. Clock geometry, hide-completed reflow, leftover shares, and rotation that does not edit stored times stay as specified.

## Impact

- `values/strings.xml` and `values-ru/strings.xml` (`dial_am`, `dial_pm`, `cd_half_am`, `cd_half_pm`).
- `AmPmRow` KDoc. The row and `DualTimeDial` already bind those resources.
- A unit test that the content-description strings do not contain AM/PM, and that a 9:00 slice keeps its minutes when the other half is selected.
- No Room change, no new dependency, no dial rewrite.
