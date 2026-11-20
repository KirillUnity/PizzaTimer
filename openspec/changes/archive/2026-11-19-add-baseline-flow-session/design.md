# Design

## Context

See proposal.md. Pause already closes the open interval before the slip is committed. The note is a checkpoint, not a rewrite of the interval.

## Goals / Non-Goals

**Goals:**

- Record pause, resume checkpoint, and day-close duration as they behave now.

**Non-Goals:**

- Persisting mood on the interval, FSS-2 scoring, or a new screen.

## Decisions

- The spec does not require the selected mood to be stored. The slip defaults a mood in the session UI; commit stores the note as the checkpoint.

## Risks / Trade-offs

- [Mood looks persisted] → The requirement names the note and the intervals only.
