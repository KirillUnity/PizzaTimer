# Design

## Context

`DayViewModel.startTimer` creates a `Task` with `title = ""` when nothing is selected and no interval is open. `CompletionDraft.from` sets `titleEditable` only when that title is blank, so the finish sheet is the first place a name appears. The hole already prints `task.title`. See proposal.md for why the empty title is the problem.

## Goals / Non-Goals

**Goals:**

- One pure function owns the `dd.MM.yyyy HH:mm` stamp so the view model and the sheet use the same string.
- Detect a provisional title by comparing the stored title to that stamp. No new Room column.

**Non-Goals:**

- Renaming a task the user already selected.
- Changing the duration sheet, interval append rules, or the task editor.

## Decisions

1. **`TimeMath.provisionalTitle(date, startMinute)`** in `kit.core` formats the day as `dd.MM.yyyy` and the minute with the existing `formatMinuteOfDay`. The title is stored data, so it is not an Android string resource. Alternative: a UI-only placeholder. Rejected, because the dial and a later process would still see an empty title.

2. **Create path only.** `startTimer` sets that title when it builds the new task from the visible date and `now`. A selected task is passed through unchanged.

3. **Editable when blank or when the title still equals the stamp** for `task.date` and the earliest block start. `CompletionDraft.from` and the draft built in `finishBreak` both use that check. `submitCompletion` already writes `draft.title` when `titleEditable` is true and refuses a blank name.

4. **Earliest block start** is the comparison minute. Play appends intervals, so the first start stays the stamp even after pause and resume.

## Risks / Trade-offs

- [A user-typed title that happens to equal the stamp stays editable] → Same string the user can keep or replace; no extra field.
- [Two tasks can share one minute's stamp] → Titles are not unique today; this does not add a uniqueness rule.

## Migration Plan

None. Existing empty-title tasks stay empty and remain editable on the sheet.

## Open Questions

None.
