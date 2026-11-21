# Tasks

## 1. Length, close, and chime

- [x] 1.1 Add `SessionDuration` (40 minutes offers 30, no end offers 25), a duration sheet that confirms `StartFor`, a budget that closes the open interval on that minute once, a quiet minutes-left line, and hide the day-done line while a session is open. Play `session_chime.wav` once from the route, with no notification channel. Verify with `SessionDurationTest`, `DayViewModelTest.timedStart_closesOnEndMinuteOnce`, and `gradlew.bat :app:testDebugUnitTest` before archive.

## Workflow follow-up

- Archive with `openspec archive play-timer-chime --yes` only after `:app:testDebugUnitTest` passes and the box above is checked.
