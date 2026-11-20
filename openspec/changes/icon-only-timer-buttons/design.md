# Design

## Context

See proposal.md. Both hole composables already draw an icon and a label under it.

## Goals / Non-Goals

**Goals:**

- Drop the visible labels. Leave the content descriptions.

**Non-Goals:**

- Changing start, pause, or the break flow.

## Decisions

- The words stay in `cd_timer_start` and `cd_timer_pause`. The visible `timer_start` and `timer_pause` strings are no longer drawn.

## Risks / Trade-offs

- [A sighted user misses the word] → The play and pause glyphs are the control, and TalkBack still speaks the name.
