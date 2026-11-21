# Design

## Context
`prepareArcLabels` sets `hOffset` to half the arc and `DialView` paints with `Align.CENTER`. That combination stacks glyphs at the sector midpoint.

## Goals / Non-Goals
- Goal: one readable title along each sector arc.
- Non-goals: pizza wedge titles, leftover math, allocations in `draw()`.

## Decisions
1. **`drawTextOnPath` uses `Paint.Align.LEFT`.** Restore the previous align after the call so empty-path `drawText` can stay centered.
2. **Center the string with `hOffset = (arcLength - measuredWidth) / 2`.** Ellipsis still uses `labelWidthFractionOfArc`.
3. **Keep the lower-half reverse path.** Upright glyphs do not change.

## Risks / Trade-offs
- [Slight width difference vs CENTER] → `measureText` is independent of align.

## Migration Plan
None.

## Open Questions
None.
