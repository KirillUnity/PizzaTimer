# Proposal: Unique periods, editable and removable

## Why
The task list shows duplicate `#break` rows with the same clock range. Users can stack the same hours on one day, and an interval cannot be removed from the editor even though Remove already exists as an intent.

## What Changes
- Break wedges stay off the task list and tag chips.
- Saving a timed task MUST fail when its periods overlap another task on that day, or overlap each other.
- Unclosed intervals can be edited with a time picker and removed. Closed intervals stay locked.

## Capabilities
- `task-list`: hide breaks; reject overlapping periods; edit/remove unclosed intervals.

## Impact
`visibleTasks`, `TaskDraftValidator`, `IntervalRow`, time picker overlay.
