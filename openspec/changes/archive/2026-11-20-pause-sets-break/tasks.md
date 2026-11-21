# Tasks

## 1. Break instead of the slip

- [x] 1.1 On Pause, close the open work interval, start one sand break from the current minute, and leave the stop-note slip closed. Show finish-task and continue while the break is open. Continue closes the break and appends one open work interval. Finish closes the break and hides the buttons. Play on an already open interval still does not append. Update `DayViewModelTest` for that behavior. Verify with `gradlew.bat :app:testDebugUnitTest` before archive.

## Workflow follow-up

- Archive with `openspec archive pause-sets-break --yes` only after `:app:testDebugUnitTest` passes and the box above is checked.
