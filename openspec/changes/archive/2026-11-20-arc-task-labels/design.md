# Design

## Context

See proposal.md. `prepareArcLabels` currently stores a midpoint and a tangent rotation. `renderArcLabels` translates, rotates, and `drawText`. That is a chord, not an arc.

## Goals / Non-Goals

- Goal: `drawTextOnPath` along a cached `android.graphics.Path`.
- Non-goals: pizza wedge labels, changing leftover math, allocations in `draw()`.

## Decisions

1. **Build the path in `prepareArcLabels`.** `drawWithCache` already calls it. `draw()` only reads the path.
2. **Reverse the arc when the midpoint is the lower canvas half** (`0°..180°`, 3 o’clock through 9). Same predicate as today’s `+180°` flip.
3. **Keep `Paint.Align.CENTER` and `hOffset = arcLength / 2`** so a short name on a wide leftover still sits at the visual middle, now curved.

## Risks / Trade-offs

- [Very wide leftover spreads a short word only a little] → Letters still follow the circle; that is the requested look.
- [Path per sector] → Built in cache, not in `draw()`.

## Migration Plan

None.

## Open Questions

None.
