# Proposal

## Why

The five crafts of the live day (dial, leftover shares, start, pause checkpoint, day close) already exist, but the app has no place that records which of them the person has actually tried. Mission §7 (repeatable tutorials, authoring, sharing) stays out of this build. This change serves the shipped instrument behind mission §2 and §5.2–§5.4: a calm reading of real actions, not a course.

## What Changes

- Add a Practice row in Settings. The hub, five lessons, and the week strip open inside Settings. Settings stays the selected top-level destination. No fifth tab.
- Mark five skills «tried» with DataStore flags on real actions. Flags are never cleared.
- Show Monday–Sunday closed minutes for the local week that includes today. An open timer is excluded. An empty week uses the calm «no closed intervals» line.
- Lessons explain the canon in words and static diagrams. They do not mount a second live dial and do not recompute leftover 5:3:2 angles.
- No streaks, XP, or confetti.

Already shipped and unchanged: pause checkpoint text and mood, day-close memory, closed-block immutability, leftover layout weights, four navigation destinations, and the absence of the §7 tutorial library.

## Capabilities

### New Capabilities

- `instrument-practice`: Settings entry, hub progress, five tried-flags, lessons, and the week of closed minutes.

### Modified Capabilities

- `learning-library`: The practice hub is an instrument reading, not the tutorial course. The course (authoring, download, repeatable passes) remains absent. Top-level destinations stay four.

## Impact

- New `PracticeProgressStore` (DataStore) and a pure week-minute function in `kit.core`.
- Hooks on dial sector select, untimed task save, successful Start, non-empty pause commit, and day close.
- Settings navigation and Compose screens. Strings in `values` and `values-ru`.
- No Room schema change, no new Gradle module, no `kit.render` dependency on DataStore.
