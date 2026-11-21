# Design

## Context
`DayViewModel.observeDay` calls `seedIfEmpty`. Debug builds write four fixture tasks once per install. The DataStore flag then blocks a second insert but leaves the rows in Room.

## Goals / Non-Goals
- Goal: never insert the sample day; delete fixture ids on load.
- Non-goals: wiping the whole Room database, removing Preview fixtures, Play-created untitled work.

## Decisions
1. **`seedIfEmpty` only deletes** tasks whose ids are `sleep`, `work`, `lunch`, and `gym`.
2. **Ignore the handled flag for deletion** so an install that already seeded still drops those rows.
3. **Keep `SampleTasks` for Compose Previews and unit fixtures.**

## Risks / Trade-offs
- [A user task reused those ids] → The editor assigns UUIDs; only the debug seeder used those strings.

## Migration Plan
None. Rows go away on the next day-screen load.

## Open Questions
None.
