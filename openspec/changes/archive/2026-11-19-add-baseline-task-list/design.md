# Design

## Context

See proposal.md. The behavior already ships. This change only records it.

## Goals / Non-Goals

**Goals:**

- One capability, `task-list`, whose scenarios match current start, pause, and leftover sharing.

**Non-Goals:**

- Editing tasks, the database schema, or the dial.

## Decisions

- Preservation only. The alternative, rewriting the timer while documenting it, would change shipped behavior during bootstrap.

## Risks / Trade-offs

- [Spec drifts from code] → A later behavior change goes through a new change instead of a silent spec edit.
