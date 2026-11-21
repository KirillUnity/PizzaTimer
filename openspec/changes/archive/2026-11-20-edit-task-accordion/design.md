# Design

## Context

See proposal.md. `TaskEditorSheet` currently renders the full form when `createStage` is null.

## Goals / Non-Goals

- Goal: collapsed menu on edit; expand to the same fields.
- Non-goal: changing create, validation, or interval rules.

## Decisions

1. **`EditSection` + `expandedEditSections`.** Independent expand, all closed on open.
2. **Toggle intent.** Same row a second time collapses it.
3. **Validation opens the relevant section** so a blank title is not hidden.

## Risks / Trade-offs

- [Fields are one tap away] → Save still writes the whole draft.

## Migration Plan

None.

## Open Questions

None.
