# Tasks

## 1. Empty subtask

- [x] 1.1 Add `TaskInstances.daySubtask` for a Daily or Weekdays template: new id, target date, no intervals, no recurrence, the template’s series id. Point `materialize` at it, and verify `RecurrenceMaterializerTest.daily_materializesMissingTomorrowOnly` asserts Tuesday’s row has no intervals while Monday keeps its interval, and `weekdays_skipsSaturday` still adds nothing on Saturday
- [x] 1.2 Add `withTemplateFace` that copies title, description, color, importance, project, and tags from the template and keeps the subtask’s id, date, blocks, and status, and verify a unit test that a renamed template changes the shown title and leaves the subtask interval in place

## 2. Move

- [x] 2.1 Use one place-recurring path from the day screen and the task list: a Daily or Weekdays template stays on its date, a missing target date gains one empty subtask, an existing series row is left as stored. Leave every other move on `moved`, and verify a view-model test for the daily move plus the existing `TasksViewModelTest.confirmMove_changesObservedDay`

## 3. Dated intervals

- [x] 3.1 Add `intervalDate` to the task editor. A Daily or Weekdays template shows no interval list until a date is chosen; that date then loads and saves only its own row, creating the subtask on the first save when it is missing. A subtask opens on its own date. Verify an editor test that an interval saved on the template date does not appear on the other date

## 4. Integration

- [x] 4.1 Apply `withTemplateFace` on the day list and the task list, and verify a view-model test that renaming the template updates the subtask title on the day without changing its intervals
- [x] 4.2 Run `:app:testDebugUnitTest` and verify it passes before archive

## Workflow follow-up

- Archive `daily-subtask-intervals` after the unit tests pass.
