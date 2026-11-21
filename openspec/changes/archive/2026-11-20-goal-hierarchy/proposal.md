# Proposal

## Why

The completion sheet can name a next task, but there is no goal above that task. Mission step а and section 10 ask for one main goal and several intermediate goals, with a task able to sit under an intermediate one. The list stays off the dial while a session is open.

## What Changes

- One main goal and several intermediate goals, stored on the device.
- A task may link to one intermediate goal. A task with no link still saves.
- The next task created from the completion sheet inherits the current task’s intermediate goal.
- The goal list is in Settings, not on the dial. An open session does not put that list on the dial.

## Capabilities

### New Capabilities

- `goals`: one main goal, intermediate goals, task links, and inheritance.

### Modified Capabilities

- `task-list`: a task without an intermediate goal still saves, and the next task inherits the link.

## Impact

- `GoalBook` and a DataStore store. No Room version change, so the local database is not wiped.
- Settings list, completion-sheet inheritance, `GoalBookTest`.
