# Design

## Context

See proposal.md. `LazyVerticalGrid` `item { }` hosts a single cell. Several children without a layout parent are stacked (box-like). `header()` currently emits `PaperDateHeader`, `PlanGoalEntry`, and `ProjectControls` as siblings.

## Goals / Non-Goals

- Goal: one vertical stack for the header.
- Non-goal: changing planner copy, filters, or the FAB.

## Decisions

1. **Wrap the header item in `Column`.** That is the smallest fix. Alternative: wrap inside the `header` lambda in `TasksScreen`. The grid item is the shared parent, so the wrap belongs there.

## Risks / Trade-offs

- [Header grows taller] → Expected; the date strip becomes visible again.

## Migration Plan

None.

## Open Questions

None.
