# Design

## Context

See proposal.md. Idle `DialHoleContent` is a `Column` of `LiveChronoHole` plus `TimerHoleOverlay`, aligned as one group. Pizza draws a small filled hub (`outerRadius * hubRadiusFraction`). Centering the column puts Start below the hub center.

## Goals / Non-Goals

- Goal: Start/Pause at the pie center; chrono above.
- Non-goal: changing hub radius, DIAL hole layout, or numerals.

## Decisions

1. **Align the icon button to `Center`; offset chrono up.** A column cannot pin one child to the box center.
2. **Keep DIAL as a column.** The ring hole is large enough that a centered stack looks balanced.

## Risks / Trade-offs

- [Chrono sits on the pie fill] → Dark type on the sector; same as today’s overflow.

## Migration Plan

None.

## Open Questions

None.
