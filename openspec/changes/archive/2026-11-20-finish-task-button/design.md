# Design

## Context

See proposal.md. `BreakActions` is two `TextButton`s and is currently unused.

## Goals / Non-Goals

- Goal: one centered filled Finish button; Continue off the face.
- Non-goal: changing ContinueBreak in the view model or the hub Play resume path.

## Decisions

1. **Filled `Button`**, not `TextButton`.
2. **Centered** in a full-width row.

## Risks / Trade-offs

- [Continue is only on the hub] → Matches the last pause-face request.

## Migration Plan

None.

## Open Questions

None.
