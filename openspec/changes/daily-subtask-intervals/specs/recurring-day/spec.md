# Spec Delta

## Purpose

Keep one Daily or Weekdays task as the main task, and give every other day its own intervals without copying the main task’s clock.

## ADDED Requirements

### Requirement: Opening another day adds an empty subtask
When a Daily or Weekdays template occurs on a date that is not the template’s own date, and that series has no row there yet, the system MUST add one subtask for that date. The template MUST stay on its date with its intervals. The subtask MUST start with no intervals, MUST NOT repeat, and MUST belong to the template’s series. A second open of that date MUST NOT add another row. A date the rule does not occur on MUST NOT gain a row from merely opening it. `RecurrenceMaterializerTest.daily_materializesMissingTomorrowOnly` and `RecurrenceMaterializerTest.weekdays_skipsSaturday` lock the single row and the skipped day.

#### Scenario: Tomorrow opens without the template’s intervals
- **WHEN** a Daily template on Monday has one interval and Tuesday is opened
- **THEN** Tuesday has one subtask with no intervals, Monday still has the template and its interval, and opening Tuesday again does not add a second subtask

#### Scenario: A weekday rule skips a day it does not occur on
- **WHEN** a Weekdays template does not occur on Saturday and Saturday is opened
- **THEN** no subtask is added for Saturday

### Requirement: Moving a recurring template adds a subtask
Moving a Daily or Weekdays template to any other date MUST leave the template and its intervals on their date and MUST add one subtask on the target date when that series has no row there. The new subtask MUST start with no intervals. If that series already has a row on the target date, Move MUST leave that row and its intervals unchanged. Moving a task that is not Daily or Weekdays MUST keep the current move: same id, new date, same intervals. `TasksViewModelTest.confirmMove_changesObservedDay` and `TaskInstancesTest.moved_keepsIdBlocksAndProject` lock the non-recurring move.

#### Scenario: Move a daily template to another date
- **WHEN** the user moves a Daily template to a date other than its own
- **THEN** the template remains on its date with its intervals, and the target date has one subtask with no intervals in the same series

#### Scenario: Move onto a day that already has the series
- **WHEN** the user moves a Daily or Weekdays template onto a date that already has that series
- **THEN** the existing row and its intervals stay, and no second subtask is added

#### Scenario: A one-day task still moves as one row
- **WHEN** the user moves a task that is neither Daily nor Weekdays
- **THEN** that same task is on the new date with the same intervals

### Requirement: A subtask shows the template face
On the day and in the task list, a subtask MUST show the template’s title, description, color, importance, project, and tags. Its intervals, date, and completion status MUST stay its own. Changing the template’s title MUST change the title shown for an existing subtask without changing that subtask’s intervals.

#### Scenario: Renaming the template renames the day
- **WHEN** a subtask has its own interval and the template title changes
- **THEN** the subtask’s day shows the new title and the same interval

### Requirement: Template intervals need a date
Opening a Daily or Weekdays template to edit intervals MUST ask for a date before any interval is shown or changed. After a date is chosen, the editor MUST show and save only that date’s intervals: the template’s intervals when the date is the template’s date, otherwise the subtask’s intervals. Choosing a date with no subtask yet MUST create that subtask when an interval is saved, and MUST NOT copy intervals onto any other date. Opening a subtask MUST edit that subtask’s intervals without asking for another date.

#### Scenario: No intervals until a date is chosen
- **WHEN** the user opens a Daily template’s intervals and has not chosen a date
- **THEN** no interval list is shown and no interval is written

#### Scenario: The chosen date keeps its own intervals
- **WHEN** the user chooses the template’s date, adds an interval, then chooses another date
- **THEN** the new interval is only on the template’s date, and the other date shows its own intervals
