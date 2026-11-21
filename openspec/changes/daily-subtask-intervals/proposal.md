# Proposal

## Why

A daily or weekday task is one repeating front (mission §2.1, §5.2), but today the next day receives a copy of the same intervals, and Move relocates that single row. Each day needs its own intervals while the name and the rest of the task stay the main task.

## What Changes

- Moving a Daily or Weekdays template onto any other date keeps the template where it is and adds a subtask on the target date. Opening a date that the rule occurs on does the same when that subtask is missing.
- The subtask stores its own intervals, starting empty. Title, description, color, importance, project, and tags are read from the template.
- Opening a Daily or Weekdays template for its intervals asks for a date first. That date’s row is what the interval editor reads and writes. A subtask opened on its own day edits that day’s intervals directly.
- A task that is not Daily or Weekdays still moves as one row, keeping its id and intervals.

## Capabilities

### New Capabilities

- `recurring-day`: a Daily or Weekdays template stays put; each other day is a subtask with its own intervals and a face taken from the template, and the template’s interval editor is dated.

### Modified Capabilities

## Impact

`TaskInstances.materialize`, `RecurrenceMaterializer`, move on the day and task screens, and the task editor’s interval section. No Room schema change and no new dependency.

Already shipped and unchanged: non-recurring move (`TasksViewModelTest.confirmMove_changesObservedDay`, `TaskInstancesTest.moved_keepsIdBlocksAndProject`), weekday rules that skip a day (`RecurrenceMaterializerTest.weekdays_skipsSaturday`), one series row per date, closed intervals, and the template remaining on its own date when another day is opened.
