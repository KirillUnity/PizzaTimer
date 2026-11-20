# Design

## Context

See proposal.md for why. Today `DayUiEffect.ShowMessage`, `TasksUiEffect.ShowMessage`, and `ReportsUiEffect.ShowMessage` carry a finished `String`. Repository `catch` blocks use `error.message ?: "Could not …"`. Tasks and reports already have a second effect, `ShowMessageRes(resId)`, used for a failed attachment. The day effect has no resource variant. Planner refusals are already resolved through `PlannerPhrases` (empty goal, past day, window, service unavailable) and must stay on that path. `ReportsViewModelTest` asserts the Russian sentence «Сервис недоступен.»

## Goals / Non-Goals

**Goals:**

- One snackbar channel per screen: a resource id for repository failures, resolved in composition with `stringResource`.
- Day and task load banners use the same resource id as the load snackbar.
- Keep planner refusal strings on the existing localized phrase path.

**Non-Goals:**

- Translating KDoc or logs, a language picker, new validation errors, dial geometry, Room migrations.

## Decisions

1. **Resource id on the effect, not a resolved string.** ViewModels must not take an Android `Context` only to load copy. `R.string` ids are compile-time constants and are already used by `ShowMessageRes`. Alternative considered: a small reason enum mapped in the screen. That duplicates the resource table. The id is the reason.

2. **Narrow day and task effects.** Remove `ShowMessage(String)` from day and tasks. Every former English fallback becomes `ShowMessageRes`. Reports keep `ShowMessage(String)` only for `outcome.reason.message`, which is already localized. Do not add a third effect type.

3. **Resolve in composition.** `stringResource` is composable, so the route remembers the pending resource id and resolves it in composition, then shows the snackbar. `context.getString` inside `collect` is the existing attachment path; new failure copy uses `stringResource` so a preview can do the same. Previews that show a snackbar use the resource, not a literal. None do today.

4. **Load banners.** `DayUiState.errorMessage` and `TasksUiState.errorMessage` become `errorMessageRes: Int?`. The screen calls `stringResource`. On failure the ViewModel stores the load resource id, not `error.message` or the exception class name. Reports `errorMessage` stays a `String` because the planner refusal is a finished localized phrase and the reports screen does not render that field for repository exceptions. A collect or note-save exception sends `ShowMessageRes` and does not write `Exception.message` into state.

5. **Shared strings when the sentence is the same.** Duplicate, move, delete, and save-note share one name across day, tasks, and reports. Distinct actions (sample day, resize, timer, diagram, place plan, collect) keep their own names. English stays in `values`; Russian in `values-ru`.

6. **No `android.util.Log` in the catch.** These unit tests run on the JVM, where `Log` is not mocked. The exception is swallowed after the resource effect. Diagnostics stay out of the snackbar.

## Risks / Trade-offs

- [Same resource id twice in a row does not retrigger state] → clear the pending id after the snackbar is shown so the next failure, even with the same id, is visible.
- [Reports still have two effect shapes] → `ShowMessage` is only the planner phrase; repository failures use `ShowMessageRes`. Call sites are updated together.
- [Load banner and snackbar repeat the same sentence] → that is the current structure; only the source of the words changes.

## Migration Plan

Update contracts, ViewModels, and the three screens in one change. No data migration. Rollback is reverting the commit; no persisted copy.

## Open Questions

None. Planner refusals stay on `ShowMessage` / `PlannerPhrases`.
