# Design

## Context

See proposal.md for why. `TaskInstances.materialize` copies closed blocks onto the next occurrence. `TaskInstances.moved` keeps the same id and blocks and only changes the date. Both the day screen and the task list call `moved`. `RecurrenceMaterializer.ensureVisibleDay` inserts at most one series row per visible date and treats the template’s own date as that row. `TaskEditorState.toTask` saves one row, including its blocks. There is no parent-id column; `seriesId` already ties a template to its instances. Room stays on the current schema.

## Goals / Non-Goals

**Goals:**

- One helper that builds an empty-interval subtask from a Daily or Weekdays template.
- Materialize and Move share that helper. Move of any other task stays `moved`.
- Day and task lists show the template face on a subtask while keeping the subtask’s blocks, date, and status.
- The template editor withholds intervals until a date is chosen, then loads and saves only that date’s blocks.

**Non-Goals:**

- A new Room column or a migration.
- Changing weekday masks, duplicate, or delete.
- Asking for a date when the opened row is already a subtask.

## Decisions

1. **Subtask is the existing series instance with empty blocks.** `TaskInstances.daySubtask(template, date, newId)` copies identity fields the row needs (`seriesId`, `RecurrenceRule.None`, `TODO`, target date) and sets `blocks` to empty. `materialize` delegates to it instead of copying closed blocks. Alternative: a `parentId` column. Rejected because `seriesId` already points at the template and a schema bump would wipe the database.

2. **Move branches on the rule.** Daily and Weekdays call a shared `placeRecurring` used by `DayViewModel` and `TasksViewModel`: if the target date is the template date, do nothing; if `hasSeriesOnDate` is true, do nothing; otherwise upsert `daySubtask`. Any other task still uses `moved`. Alternative: relocate the template and leave a shell behind. Rejected because the template is the main task and must keep its date and intervals.

3. **Face is applied when a list is shown, not stored as the source of truth.** A pure `withTemplateFace(template)` replaces title, description, color, importance, project, and tags, and keeps id, date, blocks, status, recurrence, and seriesId. Day and task observation already hold the day’s rows; they load templates with the existing `listRecurringTemplates` and map subtasks through that function. Saving a template title therefore changes what an existing subtask shows without rewriting its blocks. Alternative: copy the face onto the subtask at creation only. Rejected because a later rename would leave the day on the old title.

4. **Interval date lives on the editor.** `TaskEditorState` gains `intervalDate: LocalDate?`. For Daily and Weekdays it starts null, the interval list stays hidden, and a date action sets it. The template’s own date loads `template.blocks`. Another date loads the series row’s blocks, or an empty list when none exists. Save writes face fields to the template and blocks only to the chosen date’s row, creating the subtask on first interval save. A subtask editor sets `intervalDate` to its date immediately and saves blocks on that row, while face edits still go to the template. Alternative: one shared block list on the template. Rejected because each day must own its intervals.

## Risks / Trade-offs

- [A list forgets to apply the face] → both day and task observation go through one function; a unit test renames the template and reads the subtask title from each view model.
- [Save writes blocks onto the template for every date] → the editor save path takes the chosen date and a test asserts the other date’s blocks are untouched.
- [An older materialized row still has copied blocks] → those rows stay; this change only stops new subtasks from starting with the template’s intervals. No wipe.

## Migration Plan

No schema change. Rollback is restoring block copy in `materialize` and the single-row `moved` path for recurring templates.
