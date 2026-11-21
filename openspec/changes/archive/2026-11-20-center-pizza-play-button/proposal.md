# Proposal

## Why

On pizza (and petals) the idle hole stacks LIVE CHRONO above Start and then centers that column. The play control sits below the white hub. The hub is small (`hubRadiusFraction` 0.18); the button must sit on the geometric center of the pie.

## What Changes

- Pizza and petals pin the Start/Pause control at `Alignment.Center`.
- LIVE CHRONO stays above the button via offset, not by centering the whole column.
- DIAL keeps the current column in the large ring hole.

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `day-dial`: the pizza/petals hub control is centered on the pie.

## Impact

- `DialHoleContent` in `SectorCallout.kt`. Optional hole-diameter helper used by `TimeDial`.
