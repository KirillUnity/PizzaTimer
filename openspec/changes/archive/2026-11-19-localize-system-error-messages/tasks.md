# Tasks

## 1. String resources

- [x] 1.1 Add failure strings in `values/strings.xml` and `values-ru/strings.xml` for day load, sample day, resize, task update, duplicate, move, timer, delete, task note, task load, task save, move onto the diagram, place the plan, report collect, and report note save. Share a name when the sentence is the same. Verify both catalogs contain every new name.

## 2. Effects and screens

- [x] 2.1 Replace day and task `ShowMessage(String)` with `ShowMessageRes`, and point report collect and note-save failures at `ShowMessageRes`. Keep report planner refusals on `ShowMessage` with the existing localized phrase. Store day and task load failures as `errorMessageRes`. Verify no ViewModel still contains the English fallback sentences and that screens resolve the id with `stringResource`.

## 3. Test

- [x] 3.1 Add a `DayViewModelTest` where a repository failure on task update emits `ShowMessageRes` with the update resource id and not an English sentence. Verify with `.\\gradlew.bat :app:testDebugUnitTest` before archive.

## Workflow follow-up

- Archive with the openspec-archive-change skill only after `:app:testDebugUnitTest` passes.
