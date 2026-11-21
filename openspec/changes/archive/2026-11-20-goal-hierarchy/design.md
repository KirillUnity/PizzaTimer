# Design

## Decisions

1. `GoalBook` holds one main title, a list of intermediate goals, and a map from task id to intermediate id. Replacing the main title does not add a second main goal.
2. The book lives in DataStore preferences, not a Room column. No schema version bump.
3. A missing map entry means the task has no intermediate goal. Saving the task does not require one.
4. When the completion sheet creates a next task, the new id copies the current task’s intermediate id when one exists.
5. The list is a section on Settings. `GoalBook.showOnDial` is false while a session is open, and the day screen does not render the list.

## Risks

- Links are keyed by task id strings. Deleting a task leaves an unused map entry. It does not move other tasks.
