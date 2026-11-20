# Design

## Context

See proposal.md. Geometry and leftover shares already live in the core layout API. Screens must keep calling that API.

## Goals / Non-Goals

**Goals:**

- Record dial geometry, reflow versus list filtering, and non-destructive rotation.

**Non-Goals:**

- A new view mode, a new renderer, or a change to importance weights.

## Decisions

- One capability, `day-dial`, covers every clock-face mode. The list is specified only where its filter differs from canvas reflow.

## Risks / Trade-offs

- [A screen recomputes angles] → The existing architecture rule still forbids that; this spec is the behavior contract.
