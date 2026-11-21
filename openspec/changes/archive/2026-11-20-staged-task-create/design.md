# Design

## Context

See proposal.md. `openCreate` currently sets `TaskEditorState` and `TaskEditorSheet` renders every field. `TaskDraftValidator` still rejects a blank title on the full editor Save.

## Goals / Non-Goals

- Goal: chooser + four skippable stages for `isNew`; one-tap today; edit stays one sheet.
- Non-goals: changing leftover weights, Play-creates-task, the planner sheet.

## Decisions

1. **`CreateStage` on the editor.** `null` means the full sheet (edit). New tasks start at `CHOOSER`.
2. **Fallback title from the UI string catalog**, passed on the quick and finish intents, so `TasksViewModel` stays free of `Context`.
3. **Skip equals Next** for optional fields. Name skip leaves the title blank until finish fills the default.

## Risks / Trade-offs

- [Default name on the dial] → Same as Play’s untitled task; the completion sheet can still ask for a name.

## Migration Plan

None.

## Open Questions

None.
