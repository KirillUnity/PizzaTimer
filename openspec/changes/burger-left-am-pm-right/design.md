# Design

## Context
The header `Row` currently draws `AmPmRow` then a spacer then the menu. The user wants the reverse.

## Goals / Non-Goals
- Goal: burger leading, AM/PM trailing on clock-face views.
- Non-goals: renaming labels, moving the date header.

## Decisions
1. **Keep one `Row` with `SpaceBetween` via spacer.** Put `IconButton` first, `AmPmRow` after the spacer when `canvasMode`.
2. **Lock order in `HalfLabelStringsTest.headerPlacesMenuThenHalves`** by reading `DayScreen.kt`.

## Risks / Trade-offs
None.

## Migration Plan
None.

## Open Questions
None.
