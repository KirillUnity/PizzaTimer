# Design

## Context

See proposal.md. Practice screens exist as design frames only.

## Goals / Non-Goals

**Goals:**

- Make the absence of the course an explicit, testable boundary.

**Non-Goals:**

- Building the course, sorting tutorials, or adding a destination.

## Decisions

- The end-to-end OpenSpec check is this spec-only change. Sorting tutorials would invent a library that the mission describes and the app does not have.

## Risks / Trade-offs

- [The gap spec is read as a ban forever] → The second requirement says a later change updates this capability.
