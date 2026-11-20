# Proposal

## Why

The start and pause controls in the dial hole carry a word under the icon. The day should show the icon alone (mission principle 7: the sitting stays quiet). The spoken name stays for accessibility.

## What Changes

- Remove the visible «Старт» and «Пауза» labels from the hole controls.
- Keep the icon and its content description.

## Capabilities

### New Capabilities

### Modified Capabilities

- `flow-session`: The start and pause controls in the hole are icon-only.

## Impact

- `TimerHoleOverlay` and `RunningSessionHole`.
- Closed intervals, start, and pause behavior stay as they are.
