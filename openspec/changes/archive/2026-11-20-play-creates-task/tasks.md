# Tasks

## 1. Play creates a task

- [x] 1.1 When Play has no selection and no open interval on the day, create one untitled task, append one open interval at the current minute, and select it. A further Play does not append or duplicate. Show the existing play icon in the idle hole, without a Start label. Verify with `DayViewModelTest` for the create and the repeat, then `gradlew.bat :app:testDebugUnitTest` before archive.

## Workflow follow-up

- Archive with `openspec archive play-creates-task --yes` only after `:app:testDebugUnitTest` passes and the box above is checked.
