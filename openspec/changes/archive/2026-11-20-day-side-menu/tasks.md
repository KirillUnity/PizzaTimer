# Tasks

## 1. Side panel

- [x] 1.1 Add `DayUiIntent.SetLook` that updates view, accent, completed color, and background on the shared appearance and keeps dark theme. Add a paper side panel on the day with those controls plus hide-completed, and remove view-mode and hide-completed chips from `DayChromeRow`. Verify the resting face has no view chips and a running session hole still shows the task name and session time.

## 2. Tests

- [x] 2.1 Add a `DayViewModelTest` that `SetLook` stores the same view, accent, and underlay Settings would read back, and that hide-completed still toggles. Leave `ReflowTest` and `VisibleListTasksTest` in place. Verify with `gradlew.bat :app:testDebugUnitTest` before archive.

## Workflow follow-up

- Archive with `openspec archive day-side-menu --yes` only after `:app:testDebugUnitTest` passes and the boxes above are checked.
