# Proposal

## Why

The dial is the shipped day view (mission principle 1). Angle math, leftover layout, hide-completed, and scene rotation are easy to break from a screen. This change records those invariants.

## What Changes

- Add a preservation spec for the day dial. No renderer or layout change.

## Capabilities

### New Capabilities

- `day-dial`: How the day is drawn on the clock, including leftover shares, hide-completed, and rotation that does not edit task times.

### Modified Capabilities

## Impact

- Locked by `TimeMathTest`, `ReflowTest`, `LeftoverLayoutTest`, `HitTestTaskTest`, and `PrepareSectorsTest`.
- List view stays a filter. Wall-clock hours of timed tasks stay civil.
