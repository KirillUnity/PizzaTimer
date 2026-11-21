# Proposal

## Why

Dial sector titles are drawn as a straight tangent at the midpoint. On a large leftover slice the name sits as a horizontal line through the hole (for example «английский» at 6 o’clock). Mission principle 1: the day should read as a clock. Titles belong on the ring, following the sector as a curve.

## What Changes

- DIAL (and petals, which share `prepareArcLabels`) draw each title with `drawTextOnPath` along the sector’s mid-radius arc.
- Lower-half arcs reverse so glyphs stay upright.
- Pizza radial helper is unchanged. Geometry and leftover shares stay the same.

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `day-dial`: task titles on the clock ring follow the sector arc.

## Impact

- `ArcLabel.kt`, `TimeDialRenderer.renderArcLabels`, `ArcLabelTest`.
