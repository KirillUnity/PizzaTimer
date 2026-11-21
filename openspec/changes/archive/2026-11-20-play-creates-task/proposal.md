# Proposal

## Why

Play on the day returns immediately when no task is selected, so the elementary action does not become work. Mission principle 1 (section 9) keeps the threshold to start as low as a single press. Mission step «а» (section 12) starts by putting a task on the day that can later be named. This change makes Play without a selection create that task and open one interval. The name is asked on the completion sheet, which is a later step.

## What Changes

- Play with nothing selected creates one untitled task on the visible day and appends one open interval at the current minute. The new task becomes the selection.
- A second Play while that interval is open does not append another interval and does not create a second task.
- A task that already has an open interval is not duplicated. Closed intervals of other tasks stay as stored.
- The idle dial hole shows the existing play icon, still without the word Start. No duration picker, sound, break, or completion sheet.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `task-list`: Start with no selection creates one task and one open interval. The existing “start while already running is ignored” rule stays.
- `flow-session`: That new open interval is a normal append-only session. Pause, notes, and closed intervals stay as specified.

## Impact

- `DayViewModel.startTimer`, the idle dial hole, and a `DayViewModelTest` for create and repeat Play.
- No Room change, no new dependency, no completion sheet.
