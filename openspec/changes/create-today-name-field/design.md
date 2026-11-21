# Design

## Context
`CreateChooser` is two buttons. `CreateTodayQuick` already accepts a fallback string. The editor already has `title` and `ChangeTitle`.

## Goals / Non-Goals
- Goal: type a name, then one tap creates an untimed task on the selected day.
- Non-goals: requiring a name (blank still uses «Новое дело»).

## Decisions
1. **Put `OutlinedTextField` on the chooser** bound to `ChangeTitle`.
2. **Keep the fallback** when the field is blank so the button still saves.

## Risks / Trade-offs
None.

## Migration Plan
None.

## Open Questions
None.
