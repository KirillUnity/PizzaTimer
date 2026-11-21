# Design

## Context

See proposal.md. `CompletionSheet` shows `completion_proxy_caption`, `completion_next`, and `flow_form_caption`.

## Goals / Non-Goals

- Goal: drop those three surfaces; 5-point prompt on the nine items.
- Non-goal: removing the 1–10 rows, the nine dimension rows, or ViewModel next-task save if `nextTitle` is set in tests.

## Decisions

1. **Hide the next field.** `nextTitle` stays on the draft so existing tests still pass; the user cannot type it.
2. **Reuse `flow_form_caption`** for the 5-point line so the nine items keep one heading.

## Risks / Trade-offs

- [Users cannot chain a next task from this sheet] → They still add a task from the list FAB.

## Migration Plan

None.

## Open Questions

None.
