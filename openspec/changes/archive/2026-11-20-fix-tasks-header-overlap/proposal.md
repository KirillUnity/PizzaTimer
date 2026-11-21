# Proposal

## Why

On the tasks tab the date strip, the “plan a goal” card, and the filters are emitted as sibling composables inside one `LazyVerticalGrid` item. A grid cell stacks children, so the headline sits on top of the card. Mission principle 1: the list must be readable at a glance.

## What Changes

- The tasks header block is a vertical `Column`, so the date strip, planner entry, and filters no longer overlap.

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `task-list`: the tasks screen header must layout its rows one under another, not stacked.

## Impact

- `TasksScreen.kt` (`ResponsiveTaskGrid` header item).
