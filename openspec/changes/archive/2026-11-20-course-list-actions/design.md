# Design

## Context

See proposal.md. `CourseScreen` prints `course_trial` above the three lessons. Spec currently forbids user-authored lessons.

## Goals / Non-Goals

- Goal: list without the trial line; two buttons; local titles persist.
- Non-goal: remote catalog, publishing, likes, or a fifth tab.

## Decisions

1. **DataStore list of id+title.** Same pattern as course history.
2. **Add is a dialog** with a title field. Blank titles are ignored.
3. **Refresh** re-reads the store into the ViewModel. No network.

## Risks / Trade-offs

- [Custom courses have no steps] → They appear in the list only; built-in lessons keep checkpoints.

## Migration Plan

None.

## Open Questions

None.
