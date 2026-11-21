# Proposal

## Why

Play with nothing selected already opens one interval, but the task title is empty, so the hole and the dial show no name until the completion sheet. Mission §5.4 needs that session to be a trackable task from the first minute. This change gives it a date-and-time title the user can replace later.

## What Changes

- Play with no selection and no open interval creates one task whose title is the visible day and the start minute, formatted `dd.MM.yyyy HH:mm` (for example `20.11.2026 17:58`), plus one open interval. That task becomes the selection.
- The completion sheet prefills that title and still lets the user replace it. A title the user already wrote stays read-only.
- A later Play does not create a second task and does not append a second interval.
- Play on an already selected task does not rename it.

## Capabilities

### New Capabilities

### Modified Capabilities

- `task-list`: Play without a selection stores a date-time title instead of an empty one, and the completion sheet can replace that provisional title.

## Impact

`DayViewModel.startTimer`, the completion draft, and `DayViewModelTest.playWithoutSelection_createsOneOpenInterval_secondPlayDoesNotAppend`. No new dependency and no Room migration.

Already shipped and unchanged: the 15-minute length sheet before the interval opens, append-only closed intervals, no completion sheet on Play, and a second Play while the interval is open.
