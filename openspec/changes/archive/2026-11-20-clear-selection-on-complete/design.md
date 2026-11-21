# Design

## Context

See proposal.md. `submitCompletion` currently writes `selectedTask = saved`.

## Goals / Non-Goals

- Goal: no selected task after a saved completion.
- Non-goal: changing ToggleComplete, which still keeps selection.

## Decisions

1. **Null selection on save.** Observe-by-id then finds nothing.

## Risks / Trade-offs

- [User cannot immediately edit the same task] → Tap the sector again.

## Migration Plan

None.

## Open Questions

None.
