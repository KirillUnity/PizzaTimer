# Proposal: Menu left, AM/PM right

## Why
The user asked to swap the header: the day-look burger on the left, AM/PM on the right.

## What Changes
- Clock-face header: menu `IconButton` at the leading edge, `AmPmRow` at the trailing edge.
- List still omits AM/PM.
- Labels stay AM/PM.

## Capabilities
- `day-dial`: header order is menu then halves.

## Impact
`DayScreen` header `Row` only.
