# Proposal

## Why

Day, task, and report snackbars still fall back to hard-coded English when a repository call fails (`Could not load the day`, `Could not save task`, `Could not place the plan`, `Could not collect the report`, and the neighboring fallbacks). Mission §4 is people using the planner in their own language; mission §5.4 is tracking the day. A Russian system locale already localizes the rest of the UI, so these fallbacks break that.

## What Changes

- Repository-failure snackbars for the day, the task list, and reports carry a string resource id. The screen resolves it with `stringResource` (system locale: `values` and `values-ru`).
- `Exception.message` is not shown in those snackbars. Platform text stays out of the UI.
- The day and task load banners use the same resource ids instead of the exception text.
- **BREAKING:** `DayUiEffect.ShowMessage(String)` and `TasksUiEffect.ShowMessage(String)` go away. Those screens only receive a resource id. Reports keep `ShowMessage(String)` for planner phrases that are already localized.
- Planner refusals (empty goal, past day, window, service unavailable) stay on their current localized copy. No in-app language picker.

## Capabilities

### New Capabilities

- `failure-messages`: System-language copy for repository failures on the day, tasks, and reports.

### Modified Capabilities

- None. Dial geometry, closed intervals, flow session, and the learning-library gap stay as specified.

## Impact

- `DayViewModel`, `TasksViewModel`, `ReportsViewModel`, their effect types, and the day, tasks, and reports screens.
- New strings in `values/strings.xml` and `values-ru/strings.xml`.
- One unit test: a repository failure emits a resource id, not an English sentence.
- No new dependency, no Room migration, no dial rewrite.
