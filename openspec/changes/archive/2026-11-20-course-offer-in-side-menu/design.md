# Design

## Context

See proposal.md. `DayScreen` currently draws a `TextButton` when `BuiltInCourse.offer` is true. `DaySideMenu` is the paper panel for view, colors, underlay, and hide-completed.

## Goals / Non-Goals

- Goal: move the offer into the panel; keep the offer gate; keep Settings course.
- Non-goal: a fifth tab, changing the three practices, showing the offer during open work.

## Decisions

1. **Same `BuiltInCourse.offer` flag.** The menu row appears only when the face button would have. Opening it closes the panel and shows `CourseScreen`.
2. **Reuse `course_offer` copy** so the control still reads «Короткая практика».

## Risks / Trade-offs

- [Offer is one extra tap] → Acceptable; the face stays for the break actions.

## Migration Plan

None.

## Open Questions

None.
